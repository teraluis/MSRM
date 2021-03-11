package core.search;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public abstract class AbstractSearchService<T extends Indexable> {
    protected final Logger logger = LoggerFactory.getLogger(AbstractSearchService.class);
    private final SearchService searchService;
    private final Class<T> typeParameterClass;

    protected AbstractSearchService(Class<T> typeParameterClass, SearchService searchService) {
        this.typeParameterClass = typeParameterClass;
        this.searchService = searchService;
    }

    protected AbstractSearchService(SearchService searchService) {
        this.typeParameterClass = null;
        this.searchService = searchService;
    }

    protected CompletionStage<PaginatedResult<List<T>>> getElasticResult(final String objectType,
                                                                         final String organization,
                                                                         Pageable pageable) {
        return this.getElasticResult(this.typeParameterClass, objectType, organization, pageable);
    }

    protected CompletionStage<PaginatedResult<List<T>>> getElasticResult(final Class<T> clazz,
                                                                         final String objectType,
                                                                         final String organization,
                                                                         Pageable pageable) {

        return searchService.list(organization, objectType, pageable)
                .thenApply(result -> executeElasticSearch(result, pageable, clazz));
    }

    protected CompletionStage<PaginatedResult<List<T>>> getElasticResult(final Class<T> clazz,
                                                                         final String[] indices,
                                                                         final String organization,
                                                                         Pageable pageable) {

        return searchService.list(organization, indices, pageable)
                .thenApply(result -> executeElasticSearch(result, pageable, clazz));
    }

    private PaginatedResult<List<T>> executeElasticSearch(Optional<JsonNode> result, Pageable pageable, final Class<T> clazz) {
        final ObjectMapper mapper = new ObjectMapper();

        if (result.isPresent()) {
            final JsonNode hits = result.get().findPath("hits").findPath("hits");
            final JsonNode total = result.get().findPath("hits").findPath("total").findPath("value");

            if (hits.isArray()) {
                final List<T> results = new ArrayList<>();

                hits.elements().forEachRemaining(node -> {
                    final JsonNode sourceNode = node.findPath("_source");
                    if (sourceNode.isObject()) {
                        try {
                            results.add(mapper.readValue(sourceNode.toString(), clazz));
                        } catch (IOException e) {
                            logger.error("Failed to parse json value", e);
                        }
                    }
                });

                // Build result with pagination header
                return new PaginatedResult<>(pageable.getPage() * pageable.getSize(), pageable.getSize(), total.asInt(), results);
            } else {
                return new PaginatedResult<>(pageable.getPage() * pageable.getSize(), pageable.getSize(), 0, new ArrayList<>());
            }
        } else {
            return new PaginatedResult<>(pageable.getPage() * pageable.getSize(), pageable.getSize(), 0, new ArrayList<>());
        }
    }
}
