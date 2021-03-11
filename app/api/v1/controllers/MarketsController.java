package api.v1.controllers;

import api.v1.forms.AddMarketCommentForm;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.typesafe.config.Config;
import core.CompletableFutureUtils;
import core.ErrorMessage;
import core.UUIDJson;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import markets.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static java.util.concurrent.CompletableFuture.supplyAsync;
import static play.libs.Json.toJson;

public class MarketsController extends Controller {
    protected static final Logger logger = LoggerFactory.getLogger(MarketsController.class);

    private final MarketsService marketsService;
    private final Config config;
    private final FormFactory formFactory;

    @Inject
    MarketsController(final MarketsService marketsService, final Config config, FormFactory formFactory) {
        this.marketsService = marketsService;
        this.config = config;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> startsWith = entries.containsKey("startsWith") ? Optional.of(entries.get("startsWith")[0]) : Optional.empty();
        Optional<Integer> page = entries.containsKey("page") ? Optional.of(Integer.parseInt(entries.get("page")[0])) : Optional.empty();
        Optional<Integer> rows = entries.containsKey("rows") ? Optional.of(Integer.parseInt(entries.get("rows")[0])) : Optional.empty();
        if (startsWith.isPresent() && page.isPresent() && rows.isPresent()) {
            return marketsService.searchPage(organization, startsWith.get(), page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(markets -> CompletableFutureUtils.sequence(markets.stream().map(market -> marketsService.serialize(organization, market).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalMarkets -> ok(toJson(finalMarkets))));
        } else if (startsWith.isPresent()) {
            return marketsService.search(organization, startsWith.get())
                    .thenCompose(markets -> CompletableFutureUtils.sequence(markets.stream().map(market -> marketsService.serialize(organization, market).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalMarkets -> ok(toJson(finalMarkets))));
        } else if (page.isPresent() && rows.isPresent()) {
            return marketsService.getPage(organization, page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(markets -> CompletableFutureUtils.sequence(
                            markets
                                    .stream()
                                    .map(market -> marketsService.serialize(organization, market).toCompletableFuture())
                                    .collect(Collectors.toList())
                    ).thenApply(finalMarkets -> ok(toJson(finalMarkets))));
        } else if (entries.containsKey("account")) {
            return marketsService.getFromAccount(organization, entries.get("account")[0])
                    // .thenCompose(markets -> CompletableFutureUtils.sequence(markets.stream().map(market -> marketsService.serialize(organization, market).toCompletableFuture()).collect(Collectors.toList()))
                    .thenApply(finalMarkets -> ok(toJson(finalMarkets)));
        } else {
            return marketsService.getAll(organization)
                    .thenCompose(markets -> CompletableFutureUtils.sequence(markets.stream().map(market -> marketsService.serialize(organization, market).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalMarkets -> ok(toJson(finalMarkets))));
        }
    }

    public CompletionStage<Result> getMarket(final String organization, final String uuid) {
        ObjectMapper mapper = Json.mapper();
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        Json.setObjectMapper(mapper);
        return marketsService.get(organization, uuid).thenCompose(market -> {
            if (market.isPresent()) {
                return marketsService.serialize(organization, market.get()).thenApply(finalMarket -> ok(toJson(finalMarket)));
            } else {
                return completedFuture(badRequest(toJson(new ErrorMessage("No market with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    //FIXME gestion de plusieurs marchés
    public CompletionStage<Result> getSingleMarket(final String organization) {
        return marketsService.getAll(organization).thenCompose(markets -> {
            if (!markets.isEmpty()) {
                return completedFuture(ok(toJson(markets.get(0))));
            } else {
                return completedFuture(badRequest(toJson(new ErrorMessage("Several markets or no market in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getBpuFile(final String organization, final String uuid, final String bpuUuid) {
        return marketsService.getBpu(bpuUuid).thenCompose(bpu -> {
            if (bpu.isPresent()) {
                if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
                    try {
                        String bucketName = organization.toLowerCase() + "-bucket";
                        String url = config.getString("minio.url");
                        String accessKey = config.getString("minio.accesskey");
                        String secretKey = config.getString("minio.secretkey");
                        MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                        if (!minioClient.bucketExists(bucketName)) {
                            minioClient.makeBucket(bucketName);
                        }
                        InputStream input = minioClient.getObject(bucketName, "crm/bpus/" + bpu.get().uuid + '/' + bpu.get().file);
                        ObjectStat stat = minioClient.statObject(bucketName, "crm/bpus/" + bpu.get().uuid + '/' + bpu.get().file);
                        System.out.println(stat.contentType());
                        return completedFuture(ok(input)
                                .as(stat.contentType())
                                .withHeader("Content-Disposition", "attachment; filename=" + bpu.get().file));
                    } catch (Exception e) {
                        logger.error("MinIO error : ", e);
                        return completedFuture(internalServerError(toJson(new ErrorMessage("Error when trying to reach minio."))));
                    }
                } else {
                    return completedFuture(internalServerError(toJson(new ErrorMessage("No minio service to create minio client."))));
                }
            } else {
                return completedFuture(badRequest(toJson(new ErrorMessage("No bpu with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> addBpuFile(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        File bpuFile = request.body().asRaw().asFile();
        if (request.contentType().isPresent()) {
            if (entries.containsKey("filename")) {
                Bpu bpu = new Bpu(Optional.empty(), entries.get("filename")[0], uuid);
                return marketsService.addBpu(organization, bpu).thenCompose(bpu1 -> {
                    if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
                        try {
                            String bucketName = organization.toLowerCase() + "-bucket";
                            String url = config.getString("minio.url");
                            String accessKey = config.getString("minio.accesskey");
                            String secretKey = config.getString("minio.secretkey");
                            MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                            if (!minioClient.bucketExists(bucketName)) {
                                minioClient.makeBucket(bucketName);
                            }
                            InputStream inputStream = new FileInputStream(bpuFile);
                            minioClient.putObject(bucketName, "crm/bpus/" + bpu.uuid + '/' + bpu.file, inputStream, bpuFile.length(), null, null, request.contentType().get());
                            inputStream.close();
                            return completedFuture(ok(toJson(bpu)));
                        } catch (Exception e) {
                            logger.error("MinIO error : ", e);
                            return completedFuture(internalServerError(toJson(new ErrorMessage("Error when trying to reach minio."))));
                        }
                    } else {
                        return completedFuture(internalServerError(toJson(new ErrorMessage("No minio service to create minio client."))));
                    }
                });
            } else {
                return completedFuture(badRequest(toJson(new ErrorMessage("Missing file name in query parameters."))));
            }
        } else {
            return completedFuture(badRequest(toJson(new ErrorMessage("Missing content-type in headers."))));
        }
    }

    public CompletionStage<Result> removeBpuFile(final String organization, final String uuid) {
        return marketsService.getBpu(uuid).thenCompose(bpu -> {
            if (!bpu.isPresent()) {
                logger.error(String.format("No bpu found with uuid : %s", uuid));
                return completedFuture(internalServerError(toJson(new ErrorMessage("No bpu found with uuid : " + uuid))));
            }
            if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
                try {
                    String bucketName = organization.toLowerCase() + "-bucket";
                    String url = config.getString("minio.url");
                    String accessKey = config.getString("minio.accesskey");
                    String secretKey = config.getString("minio.secretkey");
                    MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                    if (!minioClient.bucketExists(bucketName)) {
                        minioClient.makeBucket(bucketName);
                    }
                    minioClient.removeObject(bucketName, "crm/bpus/" + bpu.get().uuid + '/' + bpu.get().file);
                    marketsService.deleteBpu(uuid);
                    return completedFuture(ok(toJson(new UUIDJson(uuid))));
                } catch (Exception e) {
                    logger.error("MinIO error : ", e);
                    return completedFuture(internalServerError(toJson(new ErrorMessage("Error when trying to reach minio."))));
                }
            } else {
                return completedFuture(internalServerError(toJson(new ErrorMessage("No minio service to create minio client."))));
            }
        });
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        Optional<Market> market = request.body().parseJson(Market.class);

        if (!market.isPresent() || market.get().name == null || market.get().marketNumber == null) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("Market name and reference are required."))));
        } else {
            return marketsService.addMarket(organization, market.get()).thenApply(marketTmp -> marketTmp.isPresent()
                    ? ok(toJson(new UUIDJson(marketTmp.get())))
                    : internalServerError(toJson(new ErrorMessage("Error when adding market in database"))));
        }

    }

    public CompletionStage<Result> addContact(Http.Request request, final String organization, final String uuid) {
        Optional<MarketPeople> marketPeople = request.body().parseJson(MarketPeople.class);
        if (!marketPeople.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketContact is null."))));
        } else {
            try {
                return marketsService.addContact(marketPeople.get(), uuid).thenApply(market -> {
                    if (market) {
                        return ok(toJson(new UUIDJson(uuid)));
                    } else {
                        return internalServerError(toJson(new ErrorMessage("Error when adding market people to market " + uuid)));
                    }
                });
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }

    }

    public CompletionStage<Result> getContact(final String organization, final String uuid) {
        return marketsService.getContact(uuid)
                .thenCompose(markets -> CompletableFutureUtils.sequence(
                        markets
                                .stream()
                                .map(marketPeople -> marketsService.serializeMarketPeople(organization, marketPeople).toCompletableFuture())
                                .collect(Collectors.toList()
                                )
                        )
                )
                .thenApply(marketContact -> ok(toJson(marketContact)));
    }

    public CompletionStage<Result> updateContact(Http.Request request, final String organization, final String uuid, final String peopleUuid) {
        final Map<String, String[]> entries = request.queryString();
        Optional<MarketPeople> marketPeople = request.body().parseJson(MarketPeople.class);
        if (!marketPeople.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketContact is null."))));
        } else if (!entries.containsKey("role")) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("Missing role in query parameter."))));
        } else {
            try {
                return marketsService.updateContact(marketPeople.get(), uuid, peopleUuid, entries.get("role")[0])
                        .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }
    }

    public CompletionStage<Result> deleteContact(Http.Request request, final String organization, final String uuid, final String peopleUuid) {
        try {
            final Map<String, String[]> entries = request.queryString();
            if (entries.containsKey("role")) {
                return marketsService.deleteContact(uuid, peopleUuid, entries.get("role")[0])
                        .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } else {
                return CompletableFuture.completedFuture(badRequest("Role query param is necessary to delete market people."));
            }
        } catch (AssertionError e) {
            logger.info("Assertion error : ", e);
            return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
        }
    }

    public CompletionStage<Result> addAccount(Http.Request request, final String organization, final String uuid) {
        Optional<MarketEstablishment> marketAccount = request.body().parseJson(MarketEstablishment.class);
        if (!marketAccount.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketEstablishment is null."))));
        } else {
            try {
                return marketsService.addAccount(marketAccount.get(), uuid).thenApply(market -> {
                    if (market) {
                        return ok(toJson(new UUIDJson(uuid)));
                    } else {
                        return internalServerError(toJson(new ErrorMessage("Error when adding market establishment to market " + uuid)));
                    }
                });
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }

    }

    public CompletionStage<Result> getAccount(final String organization, final String uuid) {
        return marketsService.getEstablishment(uuid)
                .thenCompose(markets -> CompletableFutureUtils.sequence(
                        markets
                                .stream()
                                .map(marketAccount -> marketsService.serializeMarketEstablishment(organization, marketAccount).toCompletableFuture())
                                .collect(Collectors.toList()
                                )
                        )
                )
                .thenApply(marketAccount -> ok(toJson(marketAccount)));
    }

    public CompletionStage<Result> updateAccount(Http.Request request, final String organization, final String uuid, final String accountUuid) {
        Optional<MarketEstablishment> marketAccount = request.body().parseJson(MarketEstablishment.class);
        if (!marketAccount.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketEstablishment is null."))));
        } else {
            try {
                return marketsService.updateAccount(marketAccount.get(), uuid, accountUuid, organization)
                        .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }
    }

    public CompletionStage<Result> deleteAccount(Http.Request request, final String organization, final String uuid, final String accountUuid) {
        try {
            final Map<String, String[]> entries = request.queryString();
            if (entries.containsKey("role")) {
                return marketsService.deleteAccount(uuid, accountUuid, entries.get("role")[0])
                        .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } else {
                return CompletableFuture.completedFuture(badRequest("Role query param is necessary to delete market establishment."));
            }
        } catch (AssertionError e) {
            logger.info("Assertion error : ", e);
            return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
        }
    }

    public CompletionStage<Result> addUser(Http.Request request, final String organization, final String uuid) {
        Optional<MarketUser> marketUser = request.body().parseJson(MarketUser.class);
        if (!marketUser.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketUser is null."))));
        } else {
            try {
                return marketsService.addUser(marketUser.get(), uuid).thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }

    }

    public CompletionStage<Result> getUser(final String organization, final String uuid) {
        return marketsService.getUser(uuid)
                .thenCompose(markets -> CompletableFutureUtils.sequence(
                        markets
                                .stream()
                                .map(marketUser -> marketsService.serializeMarketUser(organization, marketUser).toCompletableFuture())
                                .collect(Collectors.toList()
                                )
                        )
                )
                .thenApply(marketUser -> ok(toJson(marketUser)));
    }

    public CompletionStage<Result> updateUser(Http.Request request, final String organization, final String uuid, final String userLogin) {
        Optional<MarketUser> marketUser = request.body().parseJson(MarketUser.class);
        if (!marketUser.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketUser is null."))));
        } else {
            try {
                return marketsService.updateUser(marketUser.get(), uuid, userLogin, organization)
                        .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }
    }

    public CompletionStage<Result> deleteUser(final String organization, final String uuid, final String userLogin) {
        try {
            return marketsService.deleteUser(uuid, userLogin)
                    .thenApply(market -> ok(toJson(new UUIDJson(uuid))));
        } catch (AssertionError e) {
            logger.info("Assertion error : ", e);
            return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
        }
    }

    public CompletionStage<Result> updateMarket(Http.Request request, final String organization, String uuid) {
        Optional<Market> market = request.body().parseJson(Market.class);
        if (!market.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(toJson(new ErrorMessage("MarketUser is null."))));
        } else {
            try {
                return marketsService.updateMarket(market.get())
                        .thenApply(marketTmp -> ok(toJson(new UUIDJson(market.get().uuid))));
            } catch (AssertionError e) {
                logger.info("Assertion error : ", e);
                return supplyAsync(() -> forbidden("Error : " + e.getMessage()));
            }
        }
    }

    public CompletionStage<Result> getReferences(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("reference")) {
            return marketsService.getReferences(uuid, entries.get("reference")[0])
                    .thenApply(references -> ok(Json.toJson(references)));
        } else if (entries.containsKey("designation")) {
            return marketsService.getReferencesFromDesignation(uuid, entries.get("designation")[0])
                    .thenApply(references -> ok(Json.toJson(references)));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Cannot request all references"))));
        }
    }

    public CompletionStage<Result> getComments(String organization, String uuid) {
        return marketsService.getComments(organization, uuid)
                .thenCompose(comments -> CompletableFutureUtils.sequence(comments.stream()
                        .map(comment -> marketsService.serializeComment(organization, comment).toCompletableFuture())
                        .collect(Collectors.toList()))
                        .thenApply(finalComments -> ok(Json.toJson(finalComments.stream().filter(Optional::isPresent)))));
    }

    public CompletionStage<Result> addComment(String organization, Http.Request request) {
        final Form<AddMarketCommentForm> commentForm = formFactory.form(AddMarketCommentForm.class);
        final Form<AddMarketCommentForm> boundForm = commentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return marketsService.addComment(organization,
                    new MarketComment(
                            Optional.empty(),
                            boundForm.get().getIdMarket(),
                            Optional.of(boundForm.get().getUser().getLogin()),
                            boundForm.get().getComment(),
                            new Date()
                    ))
                    .thenCompose(comment -> {
                        if (comment.isPresent()) {
                            return marketsService.serializeComment(organization, comment.get());
                        } else {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                    })
                    .thenApply(results -> {
                        if (results.isPresent()) {
                            return ok(Json.toJson(results.get()));
                        } else {
                            logger.error("Error during adding comment in markets");
                            return internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in markets")));
                        }
                    }).exceptionally(t -> internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in markets " + t.getMessage()))));
        }
    }
}
