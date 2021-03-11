package core.search;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.typesafe.config.Config;
import org.apache.http.HttpHost;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.support.master.AcknowledgedResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.CreateIndexResponse;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.PutMappingRequest;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;

import javax.inject.Inject;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SearchService {

    protected ObjectMapper mapper = new ObjectMapper(); // create once, reuse

    protected RestHighLevelClient elasticClient;

    protected Config config;

    protected Logger logger = LoggerFactory.getLogger(this.getClass());
    private Optional<QuerySort> querySort;

    @Inject
    SearchService(final Config config) {
        this.config = config;
        final String hostname = config.getString("elasticsearch.url");
        final int port = config.getInt("elasticsearch.port");

        elasticClient = new RestHighLevelClient(RestClient.builder(new HttpHost(hostname, port, "http")));
        mapper.registerModule(new Jdk8Module());
    }

    public <T extends Indexable> CompletionStage<Boolean> upsert(final String organization, final String objectType, final T element) {
        return CompletableFuture.supplyAsync(() -> {
            final String index = organization.toLowerCase() + "-" + objectType;

            try {
                // Serialize to JSON
                final byte[] json = mapper.writeValueAsBytes(element);

                // Insert request
                final IndexRequest indexRequest = new IndexRequest(index).id(element.getId()).source(json, XContentType.JSON);

                final UpdateRequest updateRequest = new UpdateRequest(index, element.getId()).doc(json, XContentType.JSON).upsert(indexRequest);

                try {
                    elasticClient.update(updateRequest, RequestOptions.DEFAULT);
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            } catch (JsonProcessingException e) {
                logger.error("Failed to insert order in search database", e);
                return false;
            }
        });
    }

    private CompletionStage<Boolean> checkIfIndexExists(String index) {
        return CompletableFuture.supplyAsync(() -> {
            GetIndexRequest request = new GetIndexRequest(index);
            try {
                return elasticClient.indices().exists(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                logger.error("Error while checking index exists : ", e);
                return false;
            }
        });
    }

    private CompletionStage<Boolean> createIndex(Boolean res, String index) {
        return CompletableFuture.supplyAsync(() -> {
            if (res) return true;
            CreateIndexRequest request = new CreateIndexRequest(index);
            try {
                CreateIndexResponse response = elasticClient.indices().create(request, RequestOptions.DEFAULT);
                return response.isAcknowledged();
            } catch (IOException e) {
                logger.error("Error while checking index exists : ", e);
                return false;
            }
        });
    }

    public CompletionStage<Boolean> setMapping(String organization, String objectType, Map<String, Map<String, String>> mapping) {
        final String index = organization.toLowerCase() + "-" + objectType;
        return checkIfIndexExists(index).thenCompose(res -> createIndex(res, index)).thenApply(res -> {
            PutMappingRequest request = new PutMappingRequest(index);
            final XContentBuilder builder;
            try {
                builder = XContentFactory.jsonBuilder();

                builder.startObject()
                        .startObject("properties");

                for (Map.Entry<String, Map<String, String>> entry : mapping.entrySet()) {
                    builder.startObject(entry.getKey());
                    for (Map.Entry<String, String> property : mapping.get(entry.getKey()).entrySet()) {
                        builder.field(property.getKey(), property.getValue());
                    }
                    builder.endObject();
                }

                builder.endObject()
                        .endObject();
                request.source(builder);
                AcknowledgedResponse response = elasticClient.indices().putMapping(request, RequestOptions.DEFAULT);
                return response.isAcknowledged();
            } catch (IOException e) {
                logger.error("Error during mapping ", e);
                return false;
            }
        });
    }

    public CompletionStage<Boolean> delete(final String organization, final String objectType, final String id) {
        return CompletableFuture.supplyAsync(() -> {
            final String index = organization.toLowerCase() + "-" + objectType;

            final DeleteRequest deleteRequest = new DeleteRequest(index, id);

            try {
                elasticClient.delete(deleteRequest, RequestOptions.DEFAULT);
                return true;
            } catch (IOException e) {
                logger.error("Failed to delete order search instance", e);
                return false;
            }
        });
    }

    public CompletionStage<Optional<JsonNode>> search(final String organization, final String objectType, final String query, final String[] searchableFields, final Integer size) {
        return CompletableFuture.supplyAsync(() -> {
            final String[] indices = {
                    organization.toLowerCase() + "-" + objectType,
            };

            final SearchRequest searchRequest = new SearchRequest(indices);

            searchRequest.source(new SearchSourceBuilder().query(
                    QueryBuilders.boolQuery()

                            .should(QueryBuilders.multiMatchQuery(query, searchableFields).fuzziness(Fuzziness.AUTO))
                            .should(QueryBuilders.queryStringQuery("*" + query + "*").analyzeWildcard(true))
            ).size(size));

            try {
                return Optional.of(Json.parse(elasticClient.search(searchRequest, RequestOptions.DEFAULT).toString()));
            } catch (IOException e) {
                logger.error("Fail to search", e);
                return Optional.empty();
            }
        });
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String objectType,
                                                    Pageable pageable) {

        return this.list(
                organization,
                objectType,
                pageable.getFilter(),
                pageable.getSort(),
                pageable.getPage() * pageable.getSize(),
                pageable.getSize()
        );
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String[] indices,
                                                    Pageable pageable) {

        return this.list(
                organization,
                indices,
                pageable.getFilter(),
                pageable.getSort(),
                pageable.getPage() * pageable.getSize(),
                pageable.getSize()
        );
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String objectType,
                                                    final List<QueryFilter> filters,
                                                    final Optional<QuerySort> querySort,
                                                    final int from,
                                                    final int size) {

        return this.list(
                organization,
                objectType,
                buildQueryFilters(filters),
                buildQuerySort(querySort).map(s -> new ArrayList<>(Collections.singletonList(s))).orElse(new ArrayList<>()),
                from,
                size
        );
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String[] indices,
                                                    final List<QueryFilter> filters,
                                                    final Optional<QuerySort> querySort,
                                                    final int from,
                                                    final int size) {

        return this.list(
                organization,
                indices,
                buildQueryFilters(filters),
                buildQuerySort(querySort).map(s -> new ArrayList<>(Collections.singletonList(s))).orElse(new ArrayList<>()),
                from,
                size
        );
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String objectType,
                                                    final Optional<String> queryFilters,
                                                    final List<FieldSortBuilder> builtQuerySort,
                                                    final int from,
                                                    final int size) {
        final String[] indices = {
                organization.toLowerCase() + "-" + objectType,
        };

        return this.list(
                organization,
                indices,
                queryFilters,
                builtQuerySort,
                from,
                size
        );
    }

    public CompletionStage<Optional<JsonNode>> list(final String organization,
                                                    final String[] indices,
                                                    final Optional<String> queryFilters,
                                                    final List<FieldSortBuilder> builtQuerySort,
                                                    final int from,
                                                    final int size) {


        return CompletableFuture.supplyAsync(() -> {
            final SearchRequest searchRequest = new SearchRequest(indices);

            SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder()
                    .from(from)
                    .size(size);

            if (queryFilters.isPresent()) {
                searchSourceBuilder.query(QueryBuilders.queryStringQuery(queryFilters.get()).analyzeWildcard(true));
            } else {
                searchSourceBuilder.query(QueryBuilders.matchAllQuery());
            }

            builtQuerySort.forEach(searchSourceBuilder::sort);

            searchRequest.source(searchSourceBuilder)
                    .requestCache(true);

            try {
                return Optional.of(Json.parse(elasticClient.search(searchRequest, RequestOptions.DEFAULT).toString()));
            } catch (IOException e) {
                logger.error("Fail to search", e);
                return Optional.empty();
            }
        });
    }

    protected Optional<String> buildQueryFilters(final List<QueryFilter> filters) {
        return filters.stream().map(queryItem -> queryItem.field + ":\"*" + queryItem.value + "*\"").reduce((a, b) -> a + " AND " + b);
    }


    protected Optional<FieldSortBuilder> buildQuerySort(final Optional<QuerySort> sort) {
        return sort.map(queryItem -> SortBuilders.fieldSort(queryItem.field + ".keyword").order(SortOrder.fromString(queryItem.sortOrder.toUpperCase())));
    }

    public static class QueryFilter {
        public final String field;
        public final String value;

        public QueryFilter(final String field, final String value) {
            this.field = field;
            this.value = value;
        }
    }

    public static class QuerySort {
        public final String field;
        public final String sortOrder;

        public QuerySort(final String field, final String sortOrder) {
            this.field = field;
            this.sortOrder = sortOrder;
        }
    }
}
