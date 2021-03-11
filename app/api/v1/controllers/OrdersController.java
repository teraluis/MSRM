package api.v1.controllers;

import accounts.AccountsService;
import accounts.IndexableIndividual;
import accounts.IndexableProfessional;
import api.v1.forms.*;
import api.v1.models.OrderCount;
import api.v1.models.OrderRecap;
import api.v1.models.SetOrderLines;
import bills.BillsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import core.*;
import core.search.Pageable;
import core.search.SearchService;
import establishments.EstablishmentsService;
import establishments.IndexableEstablishment;
import io.minio.MinioClient;
import io.minio.ObjectStat;
import core.models.Analyse;
import missionclient.MissionClient;
import core.models.Prestation;
import missionclient.SetPrestations;
import models.OfficeDao;
import office.OfficeService;
import orders.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Files.TemporaryFile;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import users.User;
import users.UsersService;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class OrdersController extends Controller {
    protected final static Logger logger = LoggerFactory.getLogger(OrdersController.class);

    private final OrdersService ordersService;
    private final FormFactory formFactory;
    private final Config config;
    protected final SearchService searchService;
    protected final AccountsService accountsService;
    protected final OfficeService officeService;
    protected final UsersService usersService;
    protected final EstablishmentsService establishmentsService;
    private final MissionClient missionClient;
    private final BillsService billsService;

    private CompletionStage<Result> updateOrder(Http.Request request, final String organization, final String uuid, Optional<String> login) {
        Map<String, String[]> bodyForm = request.body().asMultipartFormData().asFormUrlEncoded();
        if (!bodyForm.containsKey("name") || !bodyForm.containsKey("status") || !bodyForm.containsKey("account")) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Order name, account and status are required."))));
        } else {
            String optionalName = bodyForm.get("name")[0];
            String status = bodyForm.get("status")[0];
            String account = bodyForm.get("account")[0];
            Optional<String> market = Optional.empty();
            if (bodyForm.containsKey("market")) {
                market = Optional.of(bodyForm.get("market")[0]);
            }
            Optional<String> estimate = Optional.empty();
            if (bodyForm.containsKey("estimate")) {
                estimate = Optional.of(bodyForm.get("estimate")[0]);
            }
            Optional<String> reference_number = Optional.empty();
            if (bodyForm.containsKey("referenceNumber")) {
                reference_number = Optional.of(bodyForm.get("referenceNumber")[0]);
            }
            Optional<String> reference_file = Optional.empty();
            if (bodyForm.containsKey("referenceFile")) {
                reference_file = Optional.of(bodyForm.get("referenceFile")[0]);
            }
            Optional<Date> finalReceived = Optional.empty();
            Optional<Date> finalDeadline = Optional.empty();
            Optional<Date> finalAdviceVisit = Optional.empty();
            Optional<Date> finalAssessment = Optional.empty();
            try {
                if (bodyForm.containsKey("received")) {
                    Long received = Long.parseLong(bodyForm.get("received")[0]);
                    finalReceived = Optional.of(new Date(received));
                }
                if (bodyForm.containsKey("deadline")) {
                    Long deadline = Long.parseLong(bodyForm.get("deadline")[0]);
                    finalDeadline = Optional.of(new Date(deadline));
                }
                if (bodyForm.containsKey("adviceVisit")) {
                    Long adviceVisit = Long.parseLong(bodyForm.get("adviceVisit")[0]);
                    finalAdviceVisit = Optional.of(new Date(adviceVisit));
                }
                if (bodyForm.containsKey("assessment")) {
                    Long assessment = Long.parseLong(bodyForm.get("assessment")[0]);
                    finalAssessment = Optional.of(new Date(assessment));
                }
            } catch (Exception e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Wrong date format."))));
            }
            Optional<String> description = Optional.empty();
            if (bodyForm.containsKey("description")) {
                description = Optional.of(bodyForm.get("description")[0]);
            }
            Optional<String> workdescription = Optional.empty();
            if (bodyForm.containsKey("workdescription")) {
                workdescription = Optional.of(bodyForm.get("workdescription")[0]);
            }
            Optional<String> establishment = Optional.empty();
            if (bodyForm.containsKey("establishment")) {
                establishment = Optional.of(bodyForm.get("establishment")[0]);
            }
            Optional<String> agency = Optional.empty();
            if (bodyForm.containsKey("agency")) {
                agency = Optional.of(bodyForm.get("agency")[0]);
            }
            Optional<String> purchaserContact = Optional.empty();
            if (bodyForm.containsKey("purchaserContact")) {
                purchaserContact = Optional.of(bodyForm.get("purchaserContact")[0]);
            }
            Optional<String> commercial = Optional.empty();
            if (bodyForm.containsKey("commercial")) {
                commercial = Optional.of(bodyForm.get("commercial")[0]);
            }
            Optional<String> commentary = Optional.empty();
            if (bodyForm.containsKey("commentary")) {
                commentary = Optional.of(bodyForm.get("commentary")[0]);
            }
            Optional<String> billedEstablishment = Optional.empty();
            if (bodyForm.containsKey("billedEstablishment")) {
                billedEstablishment = Optional.of(bodyForm.get("billedEstablishment")[0]);
            }
            Optional<String> billedContact = Optional.empty();
            if (bodyForm.containsKey("billedContact")) {
                billedContact = Optional.of(bodyForm.get("billedContact")[0]);
            }
            Optional<String> payerEstablishment = Optional.empty();
            if (bodyForm.containsKey("payerEstablishment")) {
                payerEstablishment = Optional.of(bodyForm.get("payerEstablishment")[0]);
            }
            Optional<String> payerContact = Optional.empty();
            if (bodyForm.containsKey("payerContact")) {
                payerContact = Optional.of(bodyForm.get("payerContact")[0]);
            }
            orders.Order order = new orders.Order(Optional.of(uuid),
                    optionalName,
                    account,
                    OrderStatus.fromId(status),
                    null,
                    market,
                    estimate,
                    reference_number,
                    reference_file,
                    finalReceived,
                    finalDeadline,
                    finalAdviceVisit,
                    finalAssessment,
                    description,
                    workdescription,
                    purchaserContact,
                    commercial,
                    establishment,
                    commentary,
                    agency,
                    billedEstablishment,
                    billedContact,
                    payerEstablishment,
                    payerContact);
            return ordersService.update(organization, order, login).thenCompose(sameOrder -> {
                if (sameOrder.isPresent()) {
                    return ordersService.serializeOrder(organization, sameOrder.get()).thenApply(serializedOrder -> ok(Json.toJson(serializedOrder)));
                } else {
                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating order " + uuid + " in database"))));
                }
            });
        }
    }

    @Inject
    OrdersController(final OrdersService ordersService, final Config config, final FormFactory formFactory, final SearchService searchService, final AccountsService accountsService, final EstablishmentsService establishmentsService, MissionClient missionClient, BillsService billsService, OfficeService officeService, UsersService usersService) {
        this.ordersService = ordersService;
        this.formFactory = formFactory;
        this.config = config;
        this.searchService = searchService;
        this.accountsService = accountsService;
        this.establishmentsService = establishmentsService;
        this.missionClient = missionClient;
        this.billsService = billsService;
        this.officeService = officeService;
        this.usersService = usersService;
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("startsWith")) {
            return ordersService.search(organization, entries.get("startsWith")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("market")) {
            return ordersService.getFromMarket(organization, entries.get("market")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("estimate")) {
            return ordersService.getFromEstimate(organization, entries.get("estimate")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("estate")) {
            return ordersService.getFromEstate(organization, entries.get("estate")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("establishment")) {
            return ordersService.getFromEstablishment(organization, entries.get("establishment")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("account")) {
            return ordersService.getFromAccount(organization, entries.get("account")[0]).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList())).thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        } else if (entries.containsKey("name")) {
            return ordersService.checkName(organization, entries.get("name")[0]).thenApply(order -> {
                if (order.isPresent()) {
                    return ok(Json.toJson(new UUIDJson(order.get().uuid)));
                } else {
                    return ok(Json.toJson(new UUIDJson("")));
                }
            });
        } else if (entries.containsKey("type") && entries.get("type")[0].equals("count")) {
            return ordersService.getAll(organization).thenApply(orders -> {
                Integer total = orders.size();
                Integer received = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.RECEIVED.getId())).collect(Collectors.toList()).size();
                Integer filled = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.FILLED.getId())).collect(Collectors.toList()).size();
                Integer production = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.PRODUCTION.getId())).collect(Collectors.toList()).size();
                Integer billable = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.BILLABLE.getId())).collect(Collectors.toList()).size();
                Integer honored = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.HONORED.getId())).collect(Collectors.toList()).size();
                Integer closed = orders.stream().filter(o -> o.status.getId().equals(OrderStatus.CLOSED.getId())).collect(Collectors.toList()).size();
                List<Order> inProgress = orders.stream().filter(o -> !o.status.getId().equals(OrderStatus.HONORED.getId()) && !o.status.getId().equals(OrderStatus.BILLABLE.getId()) && !o.status.getId().equals(OrderStatus.CLOSED.getId())).collect(Collectors.toList());
                // 5 days deadline
                Date today = new Date();
                Long closeDeadlineTimestamp = (long) (5 * 24 * 60 * 60 * 1000);
                Integer deadlineOutdatedCount = inProgress.stream().filter(o -> o.deadline.isPresent() && o.deadline.get().before(today)).collect(Collectors.toList()).size();
                Integer deadlineCloseCount = inProgress.stream().filter(o -> o.deadline.isPresent() && o.deadline.get().after(today) && o.deadline.get().getTime() - today.getTime() < closeDeadlineTimestamp).collect(Collectors.toList()).size();
                Integer deadlineOkCount = inProgress.stream().filter(o -> o.deadline.isPresent() && o.deadline.get().after(today) && o.deadline.get().getTime() - today.getTime() > closeDeadlineTimestamp).collect(Collectors.toList()).size();
                return ok(Json.toJson(new OrderCount(total, received, filled, production, billable, honored, closed, deadlineOutdatedCount, deadlineCloseCount, deadlineOkCount)));
            });
        } else {
            return ordersService.getAll(organization).thenCompose(orders -> CompletableFutureUtils.sequence(orders.stream().map(o -> ordersService.serializeOrder(organization, o).toCompletableFuture()).collect(Collectors.toList()))
                    .thenApply(optList -> optList.stream().filter(Optional::isPresent).collect(Collectors.toList()))
                    .thenApply(optList -> optList.stream().map(Optional::get))
                    .thenApply(finalOrders -> ok(Json.toJson(finalOrders))));
        }
    }

    public CompletionStage<Result> getOrder(final String organization, final String uuid) {
        return ordersService.get(organization, uuid).thenCompose(order -> {
            if (order.isPresent()) {
                return ordersService.serializeOrder(organization, order.get()).thenApply(serializedOrder -> {
                    if (serializedOrder.isPresent()) {
                        return ok(Json.toJson(serializedOrder.get()));
                    } else {
                        return internalServerError(Json.toJson(new ErrorMessage("Error when serializing order " + uuid + " in organization " + organization)));
                    }
                });
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No order with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getReferenceFile(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
            if (entries.containsKey("file")) {
                try {
                    String fileName = entries.get("file")[0];
                    String bucketName = organization.toLowerCase() + "-bucket";
                    String url = config.getString("minio.url");
                    String accessKey = config.getString("minio.accesskey");
                    String secretKey = config.getString("minio.secretkey");
                    MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                    if (!minioClient.bucketExists(bucketName)) {
                        minioClient.makeBucket(bucketName);
                    }
                    InputStream input = minioClient.getObject(bucketName, "crm/orders/" + uuid + "/" + fileName);
                    ObjectStat stat = minioClient.statObject(bucketName, "crm/orders/" + uuid + "/" + fileName);
                    return CompletableFuture.completedFuture(ok(input)
                            .as(stat.contentType())
                            .withHeader("Content-Disposition", "attachment; filename=" + fileName));
                } catch (Exception e) {
                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when trying to reach minio."))));
                }
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Cannot get all reference files."))));
            }
        } else {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("No minio service to create minio client."))));
        }
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddOrderForm> orderForm = formFactory.form(AddOrderForm.class);
        final Form<AddOrderForm> boundForm = orderForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            try {
                if (!Optional.ofNullable(boundForm.get().getPurchaserContact()).isPresent()
                        && !Optional.ofNullable(boundForm.get().getEstablishment()).isPresent()) {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Purchaser is required."))));
                } else {
                    Optional<String> agency = Optional.empty();
                    if (!boundForm.get().getCommercial().isEmpty()) {
                        Optional<User> user = this.usersService.get(organization, boundForm.get().getCommercial()).toCompletableFuture().get();
                        if (user.isPresent()) {
                            OfficeDao office = this.officeService.getOneByName(user.get().office.get()).toCompletableFuture().get();
                            if (office != null) {
                                agency = Optional.of(office.getAgency());
                            }
                        }
                    }
                    orders.Order order = new orders.Order(
                            Optional.empty(),
                            boundForm.get().getName(),
                            boundForm.get().getAccount(),
                            OrderStatus.fromId(boundForm.get().getStatus()),
                            Optional.ofNullable(boundForm.get().getCreated()).isPresent() ? new Date(boundForm.get().getCreated()) : new Date(),
                            Optional.ofNullable(boundForm.get().getMarket()),
                            Optional.ofNullable(boundForm.get().getEstimate()),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.ofNullable(boundForm.get().getReceived()).isPresent() ? Optional.of(new Date(boundForm.get().getReceived())) : Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.empty(),
                            Optional.ofNullable(boundForm.get().getPurchaserContact()),
                            Optional.ofNullable(boundForm.get().getCommercial()),
                            Optional.ofNullable(boundForm.get().getEstablishment()),
                            Optional.empty(),
                            agency,
                            Optional.ofNullable(boundForm.get().getEstablishment()),
                            Optional.empty(),
                            Optional.ofNullable(boundForm.get().getEstablishment()),
                            Optional.empty());
                    return ordersService.add(organization, order, login).thenApply(uuid -> {
                        if (uuid.isPresent()) {
                            return ok(Json.toJson(new UUIDJson(uuid.get())));
                        } else {
                            return internalServerError(Json.toJson(new ErrorMessage("Error when adding order in database")));
                        }
                    });
                }
            } catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Invalid uuid input " + boundForm.get().getMarket()))));
            }
        }
    }

    public CompletionStage<Result> modify(Http.Request request, final String organization, final String uuid) {
        String bucketName = organization.toLowerCase() + "-bucket";
        Http.MultipartFormData<TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<TemporaryFile> referenceFileContent = body.getFile("referencefilecontent");
        Optional<String> contentType = Optional.ofNullable(body.asFormUrlEncoded().get("content-type")).map(array -> array[0]);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (referenceFileContent != null) {
            if (contentType.isPresent()) {
                if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
                    try {
                        String url = config.getString("minio.url");
                        String accessKey = config.getString("minio.accesskey");
                        String secretKey = config.getString("minio.secretkey");
                        MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                        if (!minioClient.bucketExists(bucketName)) {
                            minioClient.makeBucket(bucketName);
                        }
                        return ordersService.get(organization, uuid).thenCompose(order -> {
                            try {
                                InputStream inputStream = new FileInputStream(referenceFileContent.getRef().path().toFile());
                                minioClient.putObject(bucketName, "crm/orders/" + uuid + "/" + referenceFileContent.getFilename(), inputStream, referenceFileContent.getFileSize(), null, null, contentType.get());
                                inputStream.close();
                                referenceFileContent.getRef().path().toFile().delete();
                                return updateOrder(request, organization, uuid, login);
                            } catch (Exception e) {
                                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when trying to put file in minio."))));
                            }
                        });
                    } catch (Exception e) {
                        return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when trying to reach minio."))));
                    }
                } else {
                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("No minio service to create minio client."))));
                }
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing content type for reference file."))));
            }
        } else {
            return updateOrder(request, organization, uuid, login);
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return ordersService.delete(organization, uuid).thenApply(result -> {
            if (result.isPresent()) {
                return ok(Json.toJson(new SuccessMessage("The order has been deleted !")));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Error when deleting order in the database")));
            }
        });
    }

    public CompletionStage<Result> setOrderLinesInBatch(Http.Request request, final String organization, final String orderUuid) {
        List<SetOrderLines> setOrderLinesList = new ObjectMapper().convertValue(request.body().asJson(), new TypeReference<List<SetOrderLines>>() {
        });
        return CompletableFutureUtils.sequence(setOrderLinesList.stream().parallel().map(setOrderLines -> this.setOrderLines(setOrderLines, organization, orderUuid).toCompletableFuture()).collect(Collectors.toList()))
                .thenApply(res -> {
                    if (res.stream().allMatch(b -> b)) {
                        return ok(Json.toJson(new SuccessMessage("Batch of order lines set !")));
                    } else {
                        return internalServerError(Json.toJson(new ErrorMessage("Error during set of prestations in batch")));
                    }
                });
    }

    public CompletionStage<Result> setOrderLines(Http.Request request, final String organization, final String orderUuid) {
        SetOrderLines setOrderLines = new ObjectMapper().convertValue(request.body().asJson(), SetOrderLines.class);
        return this.setOrderLines(setOrderLines, organization, orderUuid).thenApply(done -> {
            if (done) {
                return ok(Json.toJson(new SuccessMessage("Order lines set !")));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Error during set of prestations")));
            }
        });
    }

    private CompletionStage<Boolean> setOrderLines(SetOrderLines setOrderLines, String organization, String orderUuid) {
        List<String> newOrderLines = setOrderLines.newPrestations.stream().filter(n -> Optional.ofNullable(n.orderLine.uuid).isPresent()).map(n -> n.orderLine.uuid).distinct().collect(Collectors.toList());
        List<String> orderLinesToDelete = setOrderLines.oldPrestations.stream().filter(p -> p.orderLine.isPresent()).map(p -> p.orderLine.get()).filter(p -> !newOrderLines.contains(p)).collect(Collectors.toList());

        List<OrderLine> orderLines = new ArrayList<>();
        List<Prestation> prestations = new ArrayList<>();

        setOrderLines.newPrestations.forEach(orderLineWithPrestations -> {
            OrderLine orderLine = new OrderLine(Optional.ofNullable(orderLineWithPrestations.orderLine.uuid), orderLineWithPrestations.orderLine.refadx, orderLineWithPrestations.orderLine.refbpu, orderLineWithPrestations.orderLine.designation, orderLineWithPrestations.orderLine.price, orderLineWithPrestations.orderLine.quantity, orderLineWithPrestations.orderLine.discount, orderLineWithPrestations.orderLine.tvacode, orderLineWithPrestations.orderLine.total);
            orderLines.add(orderLine);
            orderLineWithPrestations.prestations.forEach(prestation -> {
                final Analyse analyse;
                if (prestation.analyse.isPresent() && prestation.analyse.get().uuid != null && setOrderLines.analyseOrderLines.stream().anyMatch(aol -> aol.uuid.equals(prestation.analyse.get().uuid))) {
                    OrderLine analyseOrderLine = setOrderLines.analyseOrderLines.stream().filter(ol -> ol.uuid.equals(prestation.analyse.get().uuid)).findAny().map(aol -> new OrderLine(Optional.empty(),
                            aol.refadx,
                            aol.refbpu,
                            aol.designation,
                            aol.price,
                            aol.quantity,
                            aol.discount,
                            aol.tvacode,
                            aol.total)).get();
                    analyse = new Analyse(null, analyseOrderLine.uuid, prestation.analyse.get().type);
                    orderLines.add(analyseOrderLine);
                } else {
                    analyse = null;
                }
                prestations.add(new Prestation(
                        prestation.uuid,
                        prestation.status.orElse(null),
                        prestation.order.orElse(null),
                        prestation.mission.orElse(null),
                        prestation.technicalAct.orElse(null),
                        prestation.comment.orElse(null),
                        prestation.workDescription.orElse(null),
                        prestation.resultId.orElse(null),
                        prestation.diagnostician.orElse(null),
                        prestation.estate.orElse(null),
                        prestation.targetId.orElse(null),
                        orderLine.uuid,
                        analyse,
                        prestation.estateType.orElse(null),
                        prestation.billLines)
                );
            });
        });

        List<String> newPrestationsIds = setOrderLines.newPrestations.stream().flatMap(orderLineWithPrestations -> orderLineWithPrestations.prestations.stream().map(p -> p.uuid)).collect(Collectors.toList());
        List<String> prestationToDelete = setOrderLines.oldPrestations.stream().map(p -> p.uuid).filter(id -> !newPrestationsIds.contains(id)).collect(Collectors.toList());

        return ordersService.setOrderLines(organization, orderUuid, orderLines, orderLinesToDelete).thenCompose(result -> {
            if (result) {
                return missionClient.patchPrestations(organization, new SetPrestations(prestationToDelete, prestations));
            } else {
                logger.error("Error during set of order lines.");
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    public CompletionStage<Result> setStatus(Http.Request request, final String organization, final String uuid) {
        final Form<StatusForm> statusForm = formFactory.form(StatusForm.class);
        final Form<StatusForm> boundForm = statusForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return ordersService.setStatus(organization, uuid, OrderStatus.fromId(boundForm.get().getStatus()), login).thenApply((result) -> {
                if (result) {
                    return ok(Json.toJson(new SuccessMessage("Status has been updated !")));
                } else {
                    return internalServerError(Json.toJson(new ErrorMessage("Error during update of order " + uuid + " in organization " + organization)));
                }
            });
        }
    }

    public CompletionStage<Result> search(final Http.Request request, final String organization, final String indexName) {
        final Form<SearchQueryForm> searchQueryForm = formFactory.form(SearchQueryForm.class);
        final Form<SearchQueryForm> boundForm = searchQueryForm.bindFromRequest(request);

        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            // TODO: how to get IndexableIntervention.SEARCHABLE_FIELDS and IndexableEstate.SEARCHABLE_FIELDS ?
            String[] fields = {
                    "id", "name", "status", "category", "accountType", "description", "referenceNumber",
                    "address", "people.name", "people.phone", "commercial.name", "entity.name", "entity.siren",
                    "purchaser.name", "account.name", "market.name", "estate.name", "estate.address", "order.name",
                    "phone", "mail", "activity", "corporateName", "siret", "cadastralReference", "localityReference"
            };
            switch (indexName) {
                case "orders":
                    fields = IndexableOrder.SEARCHABLE_FIELDS;
                    break;
                case "professionals":
                    fields = IndexableProfessional.SEARCHABLE_FIELDS;
                    break;
                case "establishments":
                    fields = IndexableEstablishment.SEARCHABLE_FIELDS;
                    break;
                case "individuals":
                    fields = IndexableIndividual.SEARCHABLE_FIELDS;
                    break;
                default:
                    break;
            }
            final Integer maxSize = 200;
            return searchService.search(organization, indexName, boundForm.get().getQuery(), fields, maxSize).thenApply((result) -> {
                if (result.isPresent()) {
                    final JsonNode hits = result.get().findPath("hits").findPath("hits");
                    if (hits.isArray()) {
                        final List<JsonNode> results = new ArrayList<>();

                        hits.elements().forEachRemaining(node -> {
                            final JsonNode sourceNode = node.findPath("_source");
                            if (sourceNode.isObject()) {
                                results.add(sourceNode);
                            }
                        });
                        return ok(Json.toJson(results));
                    } else {
                        return ok("[]");
                    }
                } else {
                    return internalServerError(Json.toJson(new ErrorMessage("Error during search operation")));
                }
            });
        }
    }

    public CompletionStage<Result> getOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.ordersService.getOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list orders overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }

    public CompletionStage<Result> forceReindex(final String organization, final String indexName) {
        CompletionStage<Boolean> res;
        switch (indexName) {
            case "orders":
                res = ordersService.reindex(organization);
                break;
            case "accounts":
            case "professionals":
            case "individuals":
                res = accountsService.reindex(organization);
                break;
            case "establishments":
                res = establishmentsService.reindex(organization);
                break;
            case "bills":
                res = billsService.reindex(organization);
                break;
            default:
                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Invalid indexName."))));
        }

        return res.thenApplyAsync(result -> {
            if (result) {
                return ok(Json.toJson(new SuccessMessage("Search database successfully indexed.")));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Failed to index elements in search database.")));
            }
        });
    }

    public CompletionStage<Result> getComments(final String organization, final String uuid) {
        return ordersService.getComments(organization, uuid)
                .thenCompose(comments -> CompletableFutureUtils.sequence(comments.stream()
                        .map(comment -> ordersService.serializeComment(organization, comment).toCompletableFuture())
                        .collect(Collectors.toList()))
                        .thenApply(finalComments -> ok(Json.toJson(finalComments.stream().filter(Optional::isPresent)))));
    }

    public CompletionStage<Result> addComment(Http.Request request, final String organization) {
        final Form<AddOrderCommentForm> commentForm = formFactory.form(AddOrderCommentForm.class);
        final Form<AddOrderCommentForm> boundForm = commentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return ordersService.addComment(organization,
                    new OrderComment(
                            Optional.empty(),
                            boundForm.get().getIdOrder(),
                            Optional.of(boundForm.get().getUser().getLogin()),
                            boundForm.get().getComment(),
                            new Date(),
                            EventType.valueOf(boundForm.get().getEvent())))
                    .thenCompose(comment -> {
                        if (comment.isPresent()) {
                            return ordersService.serializeComment(organization, comment.get());
                        } else {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                    })
                    .thenApply(results -> {
                        if (results.isPresent()) {
                            return ok(Json.toJson(results.get()));
                        } else {
                            logger.error("Failed to add new comment in orders.");
                            return internalServerError(Json.toJson(new ErrorMessage("Failed to add new comment in orders")));
                        }
                    })
                    .exceptionally(t -> {
                        logger.error("Failed to add new comment.", t);
                        return internalServerError(Json.toJson(new ErrorMessage("Failed to add new comment in orders")));
                    });
        }
    }

    public CompletionStage<Result> orderRecap(String organization, String uuid) {
        return ordersService.get(organization, uuid).thenCompose(order -> {
            if (order.isPresent()) {
                return ordersService.serializeOrder(organization, order.get()).thenCompose(fullOrder -> {
                    if (fullOrder.isPresent()) {
                        String clientName = fullOrder.get().establishment.map(establishment -> establishment.establishment.name).orElse("Client particulier pas encore géré dans Calypso.");
                        Optional<String> marketName = fullOrder.get().market.map(market -> market.name + " - " + market.marketNumber);
                        return missionClient.listPrestationByOrderId(organization, uuid).thenCompose(prestations -> {
                            List<String> targets = prestations.stream().filter(p -> p.targetId.isPresent()).map(p -> p.targetId.get()).distinct().collect(Collectors.toList());
                            Integer estateWithoutPrestations = prestations.stream().filter(p -> !p.technicalAct.isPresent()).collect(Collectors.toList()).size();
                            Integer interventionsCreated = prestations.stream().filter(p -> p.mission.isPresent()).map(p -> p.mission.get()).distinct().collect(Collectors.toList()).size();
                            return billsService.getFromOrder(organization, uuid).thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(b -> billsService.serialize(organization, b, Optional.empty(), false).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(fullBills -> {
                                        if (fullBills.size() == 0) {
                                            return ok(Json.toJson(new OrderRecap(order.get().status.getId(), clientName, marketName, order.get().referenceNumber, order.get().referenceFile, targets, estateWithoutPrestations, interventionsCreated == 0 ? Optional.empty() : Optional.of(interventionsCreated), Optional.empty(), Optional.empty())));
                                        } else {
                                            BigDecimal total = fullBills.stream().map(b -> b.lignes.stream().map(l -> l.total).reduce(BigDecimal.ZERO, BigDecimal::add)).reduce(BigDecimal.ZERO, BigDecimal::add);
                                            return ok(Json.toJson(new OrderRecap(order.get().status.getId(), clientName, marketName, order.get().referenceNumber, order.get().referenceFile, targets, estateWithoutPrestations, interventionsCreated == 0 ? Optional.empty() : Optional.of(interventionsCreated), Optional.of(fullBills.size()), Optional.of(total))));
                                        }
                                    }));
                        });
                    } else {
                        return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Error during order " + uuid + " serialization."))));
                    }
                });
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No order with uuid " + uuid + "."))));
            }
        });
    }

    public CompletionStage<Result> addReportDestination(Http.Request r, final String o) {
        final Form<ReportDestinationForm> f = formFactory.form(ReportDestinationForm.class).bindFromRequest(r);
        if (f.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(f.errorsAsJson()));
        } else {
            return ordersService.addReportDestination(o, new ReportDestination(Optional.empty(), Optional.ofNullable(f.get().getOrder()), Optional.ofNullable(f.get().getMail()), Optional.ofNullable(f.get().getUrl()), Optional.ofNullable(f.get().getAddress()).map(AddAddressForm::getUuid), Optional.ofNullable(f.get().getPeople()).map(AddPeopleForm::getUuid), Optional.ofNullable(f.get().getEstablishment()).map(AddEstablishmentForm::getUuid))).thenApply(resp -> resp.isPresent()
                    ? ok(Json.toJson(new UUIDJson(resp.get())))
                    : internalServerError(Json.toJson(new ErrorMessage("KO"))));
        }
    }

    public CompletionStage<Result> updateReportDestination(Http.Request r, final String o) {
        final Form<ReportDestinationForm> f = formFactory.form(ReportDestinationForm.class).bindFromRequest(r);
        if (f.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(f.errorsAsJson()));
        } else {
            return ordersService.updateReportDestination(o, new ReportDestination(Optional.of(f.get().getUuid()), Optional.ofNullable(f.get().getOrder()), Optional.ofNullable(f.get().getMail()), Optional.ofNullable(f.get().getUrl()), Optional.ofNullable(f.get().getAddress()).map(AddAddressForm::getUuid), Optional.ofNullable(f.get().getPeople()).map(AddPeopleForm::getUuid), Optional.ofNullable(f.get().getEstablishment()).map(AddEstablishmentForm::getUuid))).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("OK")))
                    : internalServerError(Json.toJson(new ErrorMessage("KO"))));
        }
    }

    public CompletionStage<Result> deleteReportDestination(final String o, final String i) {
        return ordersService.deleteReportDestination(o, i).thenApply(resp -> resp
                ? ok(Json.toJson(new SuccessMessage("OK")))
                : internalServerError(Json.toJson(new ErrorMessage("KO"))));
    }
}
