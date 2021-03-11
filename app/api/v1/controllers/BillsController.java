package api.v1.controllers;

import api.v1.forms.AddBillCommentForm;
import api.v1.forms.AddPaymentForm;
import api.v1.forms.ValidateBillForm;
import api.v1.models.Avoir;
import api.v1.models.BillCount;
import api.v1.models.Paiement;
import bills.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import core.*;
import core.search.Pageable;
import estateWithAddress.EstateWithAddress;
import missionclient.Asbestos;
import missionclient.MissionClient;
import missionclient.PrestationBillLine;
import missionclient.interventions.DoneIntervention;
import missionclient.interventions.IncompleteIntervention;
import missionclient.interventions.MaterializedIntervention;
import orders.OrderStatus;
import orders.OrdersService;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import scala.Tuple3;
import utils.VariablesExport.BillCsvFileUtils;
import utils.VariablesExport.BillCsvLine;
import utils.VariablesExport.PaymentCsvLine;
import utils.VariablesExport.PaymentsCsvFileUtils;
import scala.Tuple5;
import utils.Sage100Export.BillExportUtils;
import utils.VariablesExport.*;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class BillsController extends Controller {

    private final FormFactory formFactory;
    private final BillsService billsService;
    private final OrdersService ordersService;
    private final MissionClient missionClient;

    protected final static Logger logger = LoggerFactory.getLogger(BillsController.class);

    private CompletionStage<Result> addBills(String organization, String order, Integer count) {
        Integer maxPaymentTime = 30;
        Calendar c = Calendar.getInstance();
        c.add(Calendar.DATE, maxPaymentTime);
        Stream<CompletableFuture<Optional<String>>> stream = IntStream.range(0, count).mapToObj(i -> this.billsService.add(organization, new Bill(Optional.empty(), "", false, BillStatus.PENDING, Optional.empty(), order, c.getTime(), Optional.empty())).toCompletableFuture());
        return CompletableFutureUtils.sequence(stream.collect(Collectors.toList()))
                .thenCompose(ids -> {
                    if (ids.stream().allMatch(Optional::isPresent)) {
                        return ordersService.setStatus(organization, order, OrderStatus.BILLABLE, Optional.empty()).thenApply(done -> {
                            if (done) {
                                return ok(Json.toJson(ids.stream().map(Optional::get).collect(Collectors.toList())));
                            } else {
                                return internalServerError(Json.toJson(new ErrorMessage("Error when updating order status for " + order + ".")));
                            }
                        });

                    } else {
                        return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when adding bills for " + order + "."))));
                    }
                });
    }

    private CompletionStage<Boolean> updateOrder(String organization, String order, Optional<String> login) {
        return missionClient.getInterventionsFromOrder(organization, order, new String[0])
                .thenCompose(interventions -> billsService.getFromOrder(organization, order)
                        .thenCompose(bills -> {
                            if (interventions.stream().allMatch(i -> (i.getStatus().equals("INCOMPLETE") || i.getStatus().equals("DONE")) && ((IncompleteIntervention) i).bills.size() > 0) && bills.stream().allMatch(b -> !b.status.getId().equals(BillStatus.PENDING.getId()) && !b.status.getId().equals(BillStatus.UNKNOWN.getId()))) {
                                return ordersService.setStatus(organization, order, OrderStatus.HONORED, login);
                            } else if (bills.stream().anyMatch(b -> b.status.getId().equals(BillStatus.PENDING.getId()))) {
                                return ordersService.setStatus(organization, order, OrderStatus.BILLABLE, login);
                            } else {
                                return ordersService.setStatus(organization, order, OrderStatus.PRODUCTION, login);
                            }
                        }));
    }

    @Inject
    public BillsController(final FormFactory formFactory, final BillsService billsService, final OrdersService ordersService, Config config, MissionClient missionClient) {
        this.formFactory = formFactory;
        this.billsService = billsService;
        this.ordersService = ordersService;
        this.missionClient = missionClient;
    }

    public CompletionStage<Result> getPdfBill(final String organization, final String uuid) {
        return this.billsService.generatePdfBill(organization, uuid).thenApply(pdf ->
                ok().sendBytes(pdf.getContent().toByteArray())
                        .as("application/pdf")
                        .withHeader("Content-Disposition", "attachment; filename=\"" + pdf.getName() + "\"")
        );
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("order")) {
            return billsService.getFromOrder(organization, entries.get("order")[0])
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> billsService.serialize(organization, bill, Optional.empty(), false).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalBill -> ok(Json.toJson(finalBill)));
        } else if (entries.containsKey("name")) {
            return billsService.getFromName(organization, entries.get("name")[0]).thenApply(facture -> {
                if (facture.isPresent()) {
                    return ok(Json.toJson(new UUIDJson(facture.get().uuid)));
                } else {
                    return ok(Json.toJson(new UUIDJson("")));
                }
            });
        } else if (entries.containsKey("type") && entries.get("type")[0].equals("count")) {
            return billsService.getAll(organization)
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> billsService.serialize(organization, bill, Optional.empty(), false).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(bills -> {
                        final Integer total = bills.size();
                        final Integer pending = bills.stream().filter(bill -> bill.status.equals(BillStatus.PENDING.getId())).collect(Collectors.toList()).size();
                        final Integer confirmed = bills.stream().filter(bill -> bill.status.equals(BillStatus.CONFIRMED.getId())).collect(Collectors.toList()).size();
                        final Integer billed = bills.stream().filter(bill -> bill.status.equals(BillStatus.BILLED.getId())).collect(Collectors.toList()).size();
                        final Integer cancelled = bills.stream().filter(bill -> bill.status.equals(BillStatus.CANCELLED.getId())).collect(Collectors.toList()).size();
                        final Integer paid = bills.stream().filter(bill -> bill.paiements.size() > 0).collect(Collectors.toList()).size();
                        return ok(Json.toJson(new BillCount(total, pending, confirmed, billed, cancelled, paid)));
                    });

        } else {
            return billsService.getAll(organization)
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> billsService.serialize(organization, bill, Optional.empty(), false).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalBill -> ok(Json.toJson(finalBill)));
        }
    }

    public CompletionStage<Result> getFacture(final String organization, final String factureId) {
        try {
            return billsService.get(organization, factureId).thenCompose(bill -> {
                if (bill.isPresent()) {
                    return billsService.serialize(organization, bill.get(), Optional.empty(), false)
                            .thenApply(finalBill -> ok(Json.toJson(finalBill)));
                } else {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No facture with uuid " + factureId + " found"))));
                }
            });
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        if (request.body().asJson().has("order") && request.body().asJson().has("count")) {
            String order = request.body().asJson().get("order").asText();
            Integer count = request.body().asJson().get("count").asInt();
            return addBills(organization, order, count);
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Order and count fields are required."))));
        }

    }

    public CompletionStage<Result> patch(Http.Request request, final String organization) {
        if (request.body().asJson().has("order") && request.body().asJson().has("bills")) {
            if (request.body().asJson().get("bills").isArray()) {
                String order = request.body().asJson().get("order").asText();
                List<String> billsToRemove = new ObjectMapper().convertValue(request.body().asJson().get("bills"), ArrayList.class);
                if (billsToRemove.size() > 0) {
                    return CompletableFutureUtils.sequence(billsToRemove.stream().map(billToRemove -> billsService.delete(organization, billToRemove).toCompletableFuture()).collect(Collectors.toList()))
                            .thenCompose(deleted -> {
                                if (deleted.stream().allMatch(Optional::isPresent)) {
                                    Integer maxPaymentTime = 30;
                                    Calendar c = Calendar.getInstance();
                                    c.add(Calendar.DATE, maxPaymentTime);
                                    return this.billsService.add(organization, new Bill(Optional.empty(), "", false, BillStatus.PENDING, Optional.empty(), order, c.getTime(), Optional.empty()))
                                            .thenApply(uuid -> {
                                                if (uuid.isPresent()) {
                                                    return ok(Json.toJson(new UUIDJson(uuid.get())));
                                                } else {
                                                    return internalServerError(Json.toJson(new ErrorMessage("Error when adding new bill.")));
                                                }
                                            });
                                } else {
                                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when deleting bills for " + order + "."))));
                                }
                            });
                } else {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("There must have at least one bill to remove."))));
                }
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Bills are not in the right format."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing order or bills."))));
        }

    }

    public CompletionStage<Result> delete(final String organization, final String factureId) {
        try {
            return billsService.delete(organization, factureId).thenApply(res -> {
                if (res.isPresent()) {
                    return ok(Json.toJson(new SuccessMessage("Bill " + factureId + " has been deleted !")));
                } else {
                    return badRequest(Json.toJson(new ErrorMessage("Error when deleting bill " + factureId + ".")));
                }
            });
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> addPayment(Http.Request request, final String organization, final String factureUuid) {
        final Form<AddPaymentForm> addPaymentForm = formFactory.form(AddPaymentForm.class);
        final Form<AddPaymentForm> boundForm = addPaymentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Payment payment = new Payment(Optional.empty(), boundForm.get().getType(), boundForm.get().getValue(), boundForm.get().getReceived(), new Date(boundForm.get().getDate()), Optional.empty());
            return billsService.get(organization, factureUuid).thenCompose(facture -> {
                if (facture.isPresent()) {
                    return billsService.addPaiement(organization, payment, facture.get()).thenApply(uuid -> ok(Json.toJson(new UUIDJson(uuid.toString()))));
                } else {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No bill with uuid " + factureUuid + "."))));
                }
            });
        }
    }

    // on ajoute un avoir total sur une facture : donc on a pas besoin des lignes de facture
    public CompletionStage<Result> addCreditNote(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (request.body().asJson().has("interventions") && request.body().asJson().get("interventions").isArray()) {
            List<String> interventions = new ObjectMapper().convertValue(request.body().asJson().get("interventions"), ArrayList.class);
            CreditNote creditNote = new CreditNote(Optional.empty(), "", DateTime.now().toDate(), Optional.empty());
            return billsService.addAvoir(organization, uuid, creditNote).thenCompose(avoirUuid -> {
                if (avoirUuid.isPresent()) {
                    return this.ordersService.getFromBill(organization, uuid).thenCompose(order -> {
                        if (order.isPresent()) {
                            return ordersService.serializeOrder(organization, order.get()).thenCompose(serializedOrder -> {
                                if (serializedOrder.isPresent()) {
                                    Integer maxPaymentTime = serializedOrder.get().estimate.isPresent() && serializedOrder.get().estimate.get().account.isPresent() ? serializedOrder.get().estimate.get().account.get().maxPaymentTime.orElse(0) : 0;
                                    Calendar c = Calendar.getInstance();
                                    c.add(Calendar.DATE, maxPaymentTime);
                                    return this.billsService.add(organization, new Bill(Optional.empty(), "", false, BillStatus.PENDING, Optional.empty(), order.get().uuid, c.getTime(), Optional.empty())).thenCompose(newBillUuid -> {
                                        if (newBillUuid.isPresent()) {
                                            return CompletableFutureUtils.sequence(interventions.stream().map(i -> missionClient.patchBill(organization, i, newBillUuid.get()).toCompletableFuture()).collect(Collectors.toList()))
                                                    .thenCompose(updated -> {
                                                        if (updated.stream().allMatch(u -> u)) {
                                                            return this.billsService.setStatus(organization, uuid, BillStatus.CANCELLED, login).thenCompose(done -> {
                                                                if (done) {
                                                                    return ordersService.setStatus(organization, order.get().uuid, OrderStatus.BILLABLE, login).thenApply(orderDone -> {
                                                                        if (orderDone) {
                                                                            return ok(Json.toJson(new UUIDJson(avoirUuid.get())));
                                                                        } else {
                                                                            return internalServerError(Json.toJson(new ErrorMessage("Error when updating order status for " + order.get().uuid + ".")));
                                                                        }
                                                                    });
                                                                } else {
                                                                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating status for bill " + uuid + "."))));
                                                                }
                                                            });
                                                        } else {
                                                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating bill " + uuid + " for interventions."))));
                                                        }
                                                    });
                                        } else {
                                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Couldn't add new bill for ex-bill " + uuid + "."))));
                                        }
                                    });
                                } else {
                                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error during serialization of " + uuid + "."))));
                                }
                            });
                        } else {
                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error whe updating status for bill " + uuid + "."))));
                        }
                    });
                } else {
                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when adding credit note to bill " + uuid + "."))));
                }
            });
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Bad request : missing interventions."))));
        }
    }

    public CompletionStage<Result> getDetailedBills(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("order")) {
            return billsService.getFromOrder(organization, entries.get("order")[0])
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> this.billsService.serializeWithDetails(organization, bill).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(results -> ok(Json.toJson(results)));
        } else if (entries.containsKey("startsWith") && entries.containsKey("page") && entries.containsKey("rows")) {
            Integer page = Integer.parseInt(entries.get("page")[0]);
            Integer rows = Integer.parseInt(entries.get("rows")[0]);
            return billsService.searchPage(organization, entries.get("startsWith")[0], page * rows, rows + 1)
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> this.billsService.serializeWithDetails(organization, bill).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(results -> ok(Json.toJson(results)));
        } else if (entries.containsKey("page") && entries.containsKey("rows")) {
            Integer page = Integer.parseInt(entries.get("page")[0]);
            Integer rows = Integer.parseInt(entries.get("rows")[0]);
            return billsService.getPage(organization, page * rows, rows + 1)
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> this.billsService.serializeWithDetails(organization, bill).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(results -> ok(Json.toJson(results)));
        } else {
            return billsService.getAll(organization)
                    .thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> this.billsService.serializeWithDetails(organization, bill).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(results -> ok(Json.toJson(results)));
        }
    }

    /*
    public CompletionStage<Result> generateExport(Http.Request request, final String organization) {
        final Form<NameForm> nameForm = formFactory.form(NameForm.class);
        final Form<NameForm> boundForm = nameForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            String fileName = boundForm.get().getName();
            if (config.hasPath("minio.url") && config.hasPath("minio.accesskey") && config.hasPath("minio.secretkey")) {
                if (!this.export) {
                    this.export = true;
                    return billsService.generateNames(organization).thenCompose(done -> {
                        if (done) {
                            return billsService.getBillsToExport(organization).thenCompose(tuples -> {
                                try {
                                    List<Tuple5<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>>> finalTuples = tuples.stream().filter(tuple -> tuple._2().establishment.isPresent()).filter(tuple -> tuple._2().market.isPresent()).collect(Collectors.toList());
                                    Date exportDate = new Date();
                                    List<String> results = new ArrayList<>();
                                    BillExportUtils billExportUtils = new BillExportUtils();
                                    results.add("#FLG 000");
                                    results.add("#VER 20");
                                    for (Tuple5<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>> tuple : finalTuples) {
                                        List<String> lines = billExportUtils.getExportLines(
                                                tuple._1(),
                                                tuple._2(),
                                                tuple._3(),
                                                tuple._3().entity.get(),
                                                tuple._2().establishment.get(),
                                                tuple._2().commercial.get(),
                                                tuple._4().stream().map(intervention -> (DoneIntervention) intervention).collect(Collectors.toList()),
                                                exportDate,
                                                tuple._5(),
                                                tuple._2().referenceNumber
                                        );
                                        results.addAll(lines);
                                    }
                                    results.add("#FIN");
                                    if (results.size() > 3) {
                                        StringBuilder builder = new StringBuilder();
                                        for (String line : results) {
                                            builder.append(line + "\n");
                                        }
                                        InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.ISO_8859_1));
                                        String bucketName = organization.toLowerCase() + "-bucket";
                                        String url = config.getString("minio.url");
                                        String accessKey = config.getString("minio.accesskey");
                                        String secretKey = config.getString("minio.secretkey");
                                        MinioClient minioClient = new MinioClient(url, accessKey, secretKey);
                                        if (!minioClient.bucketExists(bucketName)) {
                                            minioClient.makeBucket(bucketName);
                                        }
                                        long size = inputStream.available();
                                        minioClient.putObject(bucketName, "crm/facturation/export/" + fileName, inputStream, size, null, null, "application/octet-stream");
                                        return this.billsService.setExported(organization, exportDate).thenApply(res -> {
                                            if (res) {
                                                try {
                                                    InputStream input = minioClient.getObject(bucketName, "crm/facturation/export/" + fileName);
                                                    this.export = false;
                                                    return ok(input);
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                    this.export = false;
                                                    return internalServerError(Json.toJson(new ErrorMessage("Error when trying to access file in minio : " + e.getMessage())));
                                                }
                                            } else {
                                                this.export = false;
                                                return internalServerError(Json.toJson(new ErrorMessage("Error when trying to set export in database.")));
                                            }
                                        });
                                    } else {
                                        this.export = false;
                                        return CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("Nothing to export."))));
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    this.export = false;
                                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when trying to export file : " + e.getMessage()))));
                                }
                            });
                        } else {
                            this.export = false;
                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when trying to generate bills and credit notes names for export."))));
                        }
                    });
                } else {
                    this.export = false;
                    return CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("Nothing to export."))));
                }
            } else {
                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("No minio service to create minio client."))));
            }
        }
    }
    */

    public CompletionStage<Result> exportPaymentForVariables(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("startdate") && entries.containsKey("enddate")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = dateFormat.parse(entries.get("startdate")[0]);
                Date endDate = dateFormat.parse(entries.get("enddate")[0]);
                return billsService.getExportedBills(organization, startDate, endDate).thenApply(tuples -> {
                    Integer i = 1;
                    List<PaymentCsvLine> lines = new ArrayList<>();
                    for (Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>> tuple : tuples) {
                        for (Paiement payment : tuple._1().paiements) {
                            lines.add(new PaymentCsvLine(PaymentCsvLine.generateValues((DoneIntervention) tuple._3(), tuple._1(), payment, tuple._2(), tuple._2().commercial.get(), i)));
                            i = i + 1;
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    String firstLine = Stream.of(PaymentsCsvFileUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                    builder.append(firstLine);
                    for (PaymentCsvLine csvLine : lines) {
                        builder.append(csvLine.generateLine(PaymentsCsvFileUtils.getColumns()));
                    }
                    InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(Charset.forName("windows-1252")));
                    return ok(inputStream).as("text/plain; charset=windows-1252");
                });
            } catch (ParseException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Startdate or enddate are not in the expected format (yyyy-MM-dd)."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing startdate and enddate query parameters."))));
        }
    }

    public CompletionStage<Result> exportBillForVariables(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("startdate") && entries.containsKey("enddate")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = dateFormat.parse(entries.get("startdate")[0]);
                Date endDate = dateFormat.parse(entries.get("enddate")[0]);
                return billsService.getExportedBills(organization, startDate, endDate).thenApply(tuples -> {
                    List<BillCsvLine> lines = new ArrayList<>();

                    for (Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>> tuple : tuples) {
                        for (Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>> interventionTuple : tuple._3()) {
                            lines.add(new BillCsvLine(BillCsvLine.generateValuesFromBill((DoneIntervention) interventionTuple._1(), tuple._1(), tuple._2().commercial.get(), interventionTuple._3(), tuple._2())));
                        }
                        for (Avoir creditNote : tuple._1().creditnotes) {
                            for (Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>> interventionTuple : tuple._3()) {
                                lines.add(new BillCsvLine(BillCsvLine.generateValuesFromCreditNote((DoneIntervention) interventionTuple._1(), creditNote, tuple._1(), tuple._2().commercial.get(), interventionTuple._3(), tuple._2())));
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    String firstLine = Stream.of(BillCsvFileUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                    builder.append(firstLine);
                    for (BillCsvLine csvLine : lines) {
                        builder.append(csvLine.generateLine(BillCsvFileUtils.getColumns()));
                    }
                    InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(Charset.forName("windows-1252")));
                    return ok(inputStream).as("text/plain; charset=windows-1252");
                });
            } catch (ParseException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Startdate or enddate are not in the expected format (yyyy-MM-dd)."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing startdate and enddate query parameters."))));
        }
    }

    public CompletionStage<Result> exportRecoveryForVariables(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("startdate") && entries.containsKey("enddate")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = dateFormat.parse(entries.get("startdate")[0]);
                Date endDate = dateFormat.parse(entries.get("enddate")[0]);
                return billsService.getExportedBills(organization, startDate, endDate).thenApply(tuples -> {
                    List<RecoveryCsvLine> lines = new ArrayList<>();

                    for (Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>> tuple : tuples) {
                        if (BillStatus.RECOVERY == BillStatus.fromId(tuple._1().status)) {
                            for (Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>> interventionTuple : tuple._3()) {
                                lines.add(new RecoveryCsvLine(RecoveryCsvLine.generateValuesFromBill((DoneIntervention) interventionTuple._1(), tuple._1(), tuple._2().commercial.get(), interventionTuple._3(), tuple._2())));
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    String firstLine = Stream.of(RecoveryCsvFileUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                    builder.append(firstLine);
                    for (RecoveryCsvLine csvLine : lines) {
                        builder.append(csvLine.generateLine(RecoveryCsvFileUtils.getColumns()));
                    }
                    InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(Charset.forName("windows-1252")));
                    return ok(inputStream).as("text/plain; charset=windows-1252");
                });
            } catch (ParseException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Startdate or enddate are not in the expected format (yyyy-MM-dd)."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing startdate and enddate query parameters."))));
        }
    }

    public CompletionStage<Result> exportInProgressForVariables(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("startdate") && entries.containsKey("enddate")) {
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date startDate = dateFormat.parse(entries.get("startdate")[0]);
                Date endDate = dateFormat.parse(entries.get("enddate")[0]);
                return billsService.getExportedBills(organization, startDate, endDate).thenApply(tuples -> {
                    List<InProgressCsvLine> lines = new ArrayList<>();

                    for (Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>> tuple : tuples) {
                        if (BillStatus.PENDING == BillStatus.fromId(tuple._1().status)) {
                            for (Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>> interventionTuple : tuple._3()) {
                                lines.add(new InProgressCsvLine(InProgressCsvLine.generateValuesFromBill((DoneIntervention) interventionTuple._1(), tuple._1(), tuple._2().commercial.get(), interventionTuple._3(), tuple._2(), tuple._1().paiements.get(0))));
                            }
                        }
                    }
                    StringBuilder builder = new StringBuilder();
                    String firstLine = Stream.of(InProgressCsvFileUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                    builder.append(firstLine);
                    for (InProgressCsvLine csvLine : lines) {
                        builder.append(csvLine.generateLine(InProgressCsvFileUtils.getColumns()));
                    }
                    InputStream inputStream = new ByteArrayInputStream(builder.toString().getBytes(Charset.forName("windows-1252")));
                    return ok(inputStream).as("text/plain; charset=windows-1252");
                });
            } catch (ParseException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Startdate or enddate are not in the expected format (yyyy-MM-dd)."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Missing startdate and enddate query parameters."))));
        }
    }

    public CompletionStage<Result> creditNotes(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("name")) {
            return billsService.getCreditNoteFromName(entries.get("name")[0]).thenApply(creditNote -> {
                if (creditNote.isPresent()) {
                    return ok(Json.toJson(new UUIDJson(creditNote.get().uuid)));
                } else {
                    return ok(Json.toJson(new UUIDJson("")));
                }
            });
        } else {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Cannot request all credit notes."))));
        }
    }

    public CompletionStage<Result> validate(Http.Request request, final String organization, final String uuid) {
        final Form<ValidateBillForm> validateBillForm = formFactory.form(ValidateBillForm.class);
        final Form<ValidateBillForm> boundForm = validateBillForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return billsService.get(organization, uuid).thenCompose(bill -> {
                if (bill.isPresent()) {
                    Date deadline = new Date(boundForm.get().getDeadline());
                    List<Tuple2<BillLine, List<PrestationBillLine>>> tuples = boundForm.get().getLignes().stream().map(b -> new Tuple2<>(new BillLine(Optional.empty(), b.getRefadx(), Optional.ofNullable(b.getRefbpu()), Optional.ofNullable(b.getDesignation()), b.getTvacode(), b.getPrice(), b.getQuantity(), b.getTotal(), b.getDiscount(), DateTime.now().toDate(), Optional.empty()), Optional.ofNullable(b.getPrestations()).isPresent() ? b.getPrestations() : new ArrayList<String>())).map(t -> new Tuple2<>(t._1, t._2.stream().map(prestation -> new PrestationBillLine(prestation, t._1.uuid)).collect(Collectors.toList()))).collect(Collectors.toList());
                    List<BillLine> lines = tuples.stream().map(t -> t._1).collect(Collectors.toList());
                    List<PrestationBillLine> prestationBillLines = tuples.stream().map(t -> t._2).flatMap(Collection::stream).collect(Collectors.toList());
                    Bill newBill = new Bill(Optional.of(bill.get().uuid), bill.get().name, bill.get().accompte, BillStatus.CONFIRMED, bill.get().recoverystatus, bill.get().order, deadline, bill.get().exportdate);
                    return billsService.update(organization, newBill, lines).thenCompose(updatedBill -> {
                        if (updatedBill.isPresent()) {
                            if (login.isPresent()) {
                                billsService.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>facture validée</b>", new Date(), EventType.STATUS));
                            }
                            return updateOrder(organization, updatedBill.get().order, login).thenCompose(done -> {
                                if (done) {
                                    return missionClient.patchBillLines(organization, prestationBillLines).thenCompose(missionDone -> {
                                        if (missionDone) {
                                            return billsService.serialize(organization, updatedBill.get(), Optional.empty(), false).thenApply(fullBill -> ok(Json.toJson(fullBill)));
                                        } else {
                                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error during update of prestations during validation of bill " + updatedBill.get().uuid))));
                                        }
                                    });
                                } else {
                                    return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error during update of status order " + updatedBill.get().order))));
                                }
                            });
                        } else {
                            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error during update of bill " + bill.get().uuid))));
                        }
                    });
                } else {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No bill with uuid " + uuid))));
                }
            });
        }
    }

    public CompletionStage<Result> getComments(String organization, String uuid) {
        return billsService.getComments(organization, uuid)
                .thenCompose(comments -> CompletableFutureUtils.sequence(comments.stream()
                        .map(comment -> billsService.serializeComment(organization, comment).toCompletableFuture())
                        .collect(Collectors.toList()))
                        .thenApply(finalComments -> ok(Json.toJson(finalComments.stream().filter(Optional::isPresent)))));
    }

    public CompletionStage<Result> addComment(String organization, Http.Request request) {
        final Form<AddBillCommentForm> commentForm = formFactory.form(AddBillCommentForm.class);
        final Form<AddBillCommentForm> boundForm = commentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return billsService.addComment(organization,
                    new BillComment(
                            Optional.empty(),
                            boundForm.get().getIdBill(),
                            Optional.of(boundForm.get().getUser().getLogin()),
                            boundForm.get().getComment(),
                            new Date(),
                            EventType.MESSAGE))
                    .thenCompose(comment -> {
                        if (comment.isPresent()) {
                            return billsService.serializeComment(organization, comment.get());
                        } else {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                    })
                    .thenApply(results -> {
                        if (results.isPresent()) {
                            return ok(Json.toJson(results.get()));
                        } else {
                            logger.error("Error during adding comment in bills");
                            return internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in bills")));
                        }
                    }).exceptionally(t -> internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in bills " + t.getMessage()))));
        }
    }

    public CompletionStage<Result> getOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.billsService.getOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list bill overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }
}
