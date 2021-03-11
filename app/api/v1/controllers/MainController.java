package api.v1.controllers;

import accounts.AccountsService;
import api.v1.models.*;
import bills.BillStatus;
import bills.BillsService;
import bills.Payment;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;
import com.typesafe.config.Config;
import core.CompletableFutureUtils;
import core.ErrorMessage;
import core.SuccessMessage;
import establishments.EstablishmentsService;
import estateclient.EstateClient;
import io.minio.MinioClient;
import missionclient.MissionClient;
import core.models.Prestation;
import orders.OrderStatus;
import orders.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import scala.Tuple2;
import utils.Sage1000Export.*;
import utils.Sage1000Export.PaymentParsingFile;
import utils.UserSynchronization;

import javax.inject.Inject;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class MainController extends Controller {

    protected final AccountsService accountsService;
    protected final EstablishmentsService establishmentsService;
    protected final OrdersService ordersService;
    protected final BillsService billsService;
    protected final MissionClient missionClient;
    protected final EstateClient estateClient;
    protected final FileTransfer fileTransfer;
    protected final Sage1000ExportUtils sage1000ExportUtils;
    protected final UserSynchronization userSynchroniztion;
    protected final Logger logger = LoggerFactory.getLogger(MainController.class);
    protected final Config config;

    private Boolean sendToMinio(MinioClient minioClient, String bucketName, String fileName, List<String> lines, String firstLine) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(firstLine.getBytes(StandardCharsets.ISO_8859_1));
            for (String line : lines) {
                outputStream.write(line.getBytes(StandardCharsets.ISO_8859_1));
            }
            byte[] updateByteArray = outputStream.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(updateByteArray);
            minioClient.putObject(bucketName, "test/Sage1000/" + fileName + ".txt", inputStream, new Long(updateByteArray.length), null, null, "application/octet-stream");
            inputStream.close();
            return true;
        } catch (Exception e) {
            logger.error("Error when trying to put file in minio.");
            return false;
        }
    }

    private CompletionStage<Optional<OrderTest>> get(String organization, orders.Order order) {
        System.out.println("Getting order " + order.uuid);
        return ordersService.serializeOrder(organization, order).thenCompose(finalOrder -> {
            if (finalOrder.isPresent()) {
                return this.billsService.getFromOrder(organization, order.uuid).thenCompose(bills ->
                        CompletableFutureUtils.sequence(bills.stream().map(bill -> billsService.serialize(organization, bill, Optional.empty(), false).toCompletableFuture()).collect(Collectors.toList()))
                                .thenCompose(finalBills -> missionClient.listPrestationByOrderId(organization, order.uuid).thenCompose(prestations -> {
                                    final Optional<Prestation> firstPrestation = prestations.stream().findAny();
                                    if (firstPrestation.isPresent()) {
                                        if (firstPrestation.get().estate.isPresent()) {
                                            return estateClient.getEstate(organization, firstPrestation.get().estate.get()).thenCompose(estate -> {
                                                if (firstPrestation.get().mission.isPresent()) {
                                                    return missionClient.getIntervention(organization, firstPrestation.get().mission.get()).thenCompose(intervention -> {
                                                        List<CompletableFuture<PrestationWithResult>> futurePrestations = prestations.stream().map(p -> {
                                                            if (p.resultId.isPresent()) {
                                                                return missionClient.getResult(organization, p.uuid).thenApply(result -> new PrestationWithResult(p, result)).toCompletableFuture();
                                                            } else {
                                                                return CompletableFuture.completedFuture(new PrestationWithResult(p, Optional.empty()));
                                                            }
                                                        }).collect(Collectors.toList());
                                                        return CompletableFutureUtils.sequence(futurePrestations).thenApply(finalPrestations -> Optional.of(new OrderTest(finalOrder.get(), finalBills, finalPrestations, estate, intervention)));
                                                    });
                                                } else {
                                                    List<CompletableFuture<PrestationWithResult>> futurePrestations = prestations.stream().map(p -> {
                                                        if (p.resultId.isPresent()) {
                                                            return missionClient.getResult(organization, p.uuid).thenApply(result -> new PrestationWithResult(p, result)).toCompletableFuture();
                                                        } else {
                                                            return CompletableFuture.completedFuture(new PrestationWithResult(p, Optional.empty()));
                                                        }
                                                    }).collect(Collectors.toList());
                                                    return CompletableFutureUtils.sequence(futurePrestations).thenApply(finalPrestations -> Optional.of(new OrderTest(finalOrder.get(), finalBills, finalPrestations, estate, Optional.empty())));
                                                }
                                            });
                                        } else {
                                            if (firstPrestation.get().mission.isPresent()) {
                                                return missionClient.getIntervention(organization, firstPrestation.get().mission.get()).thenCompose(intervention -> {
                                                    List<CompletableFuture<PrestationWithResult>> futurePrestations = prestations.stream().map(p -> {
                                                        if (p.resultId.isPresent()) {
                                                            return missionClient.getResult(organization, p.uuid).thenApply(result -> new PrestationWithResult(p, result)).toCompletableFuture();
                                                        } else {
                                                            return CompletableFuture.completedFuture(new PrestationWithResult(p, Optional.empty()));
                                                        }
                                                    }).collect(Collectors.toList());
                                                    return CompletableFutureUtils.sequence(futurePrestations).thenApply(finalPrestations -> Optional.of(new OrderTest(finalOrder.get(), finalBills, finalPrestations, Optional.empty(), intervention)));
                                                });
                                            } else {
                                                List<CompletableFuture<PrestationWithResult>> futurePrestations = prestations.stream().map(p -> {
                                                    if (p.resultId.isPresent()) {
                                                        return missionClient.getResult(organization, p.uuid).thenApply(result -> new PrestationWithResult(p, result)).toCompletableFuture();
                                                    } else {
                                                        return CompletableFuture.completedFuture(new PrestationWithResult(p, Optional.empty()));
                                                    }
                                                }).collect(Collectors.toList());
                                                return CompletableFutureUtils.sequence(futurePrestations).thenApply(finalPrestations -> Optional.of(new OrderTest(finalOrder.get(), finalBills, finalPrestations, Optional.empty(), Optional.empty())));
                                            }
                                        }
                                    } else {
                                        return CompletableFuture.completedFuture(Optional.of(new OrderTest(finalOrder.get(), finalBills, new ArrayList<>(), Optional.empty(), Optional.empty())));
                                    }
                                })));
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }


    private List<String> check(final OrderTest orderTest) {
        List<String> result = new ArrayList<>();
        Order order = orderTest.order;
        if (order.status.equals(OrderStatus.UNKNOWN.getId())) {
            result.add("Wrong status " + order.status + " for order.");
        } else {
            if (!order.market.isPresent() || !order.market.get().getDefaultEstablishment().isPresent()) {
                result.add("Missing client.");
            }
            if (!order.establishment.isPresent()) {
                result.add("Missing establishment.");
            }
            if (!order.purchaserContact.isPresent()) {
                result.add("Missing purchaser.");
            }
            if (!order.status.equals(OrderStatus.RECEIVED.getId())) {
                if (order.orderLines.size() == 0) {
                    result.add("Missing order lines.");
                }
                if (order.reportDestinations.size() == 0) {
                    result.add("Missing report lines.");
                }
                if (!order.referenceNumber.isPresent()) {
                    result.add("Missing reference number.");
                }
                if (!order.referenceFile.isPresent()) {
                    result.add("Missing reference file.");
                }
                if (!order.received.isPresent()) {
                    result.add("Missing received date.");
                }
                if (!order.deadline.isPresent()) {
                    result.add("Missing deadline date.");
                }
                if (!orderTest.estate.isPresent()) {
                    result.add("Missing estate for received order.");
                }
            }
            if (!order.status.equals(OrderStatus.RECEIVED.getId())
                    && !order.status.equals(OrderStatus.FILLED.getId())) {
                if (!orderTest.intervention.isPresent()) {
                    result.add("Missing intervention for production order.");
                }
                for (PrestationWithResult prestationWithResult : orderTest.prestations) {
                    if (!prestationWithResult.prestation.estate.isPresent()) {
                        result.add("Missing estate on prestation " + prestationWithResult.prestation.uuid + " for filled order.");
                    }
                    if (!prestationWithResult.prestation.orderLine.isPresent()) {
                        result.add("Missing order line on prestation " + prestationWithResult.prestation.uuid + " for filled order.");
                    }
                    if (!prestationWithResult.prestation.technicalAct.isPresent()) {
                        result.add("Missing prestation type on prestation" + prestationWithResult.prestation.uuid + " for filled order.");
                    }
                }
            }
            if (!order.status.equals(OrderStatus.RECEIVED.getId())
                    && !order.status.equals(OrderStatus.FILLED.getId())
                    && !order.status.equals(OrderStatus.PRODUCTION.getId())) {
                if (!orderTest.intervention.get().getStatus().equals("DONE")) {
                    result.add("Wrong intervention status " + orderTest.intervention.get().getStatus() + " on intervention " + orderTest.intervention.get().getId() + " for billable order.");
                }
                List<PrestationWithResult> noResults = orderTest.prestations.stream().filter(p -> !p.result.isPresent()).collect(Collectors.toList());
                for (PrestationWithResult r : noResults) {
                    result.add("Missing result on prestation " + r.prestation.uuid + " for billable order.");
                }
                if (orderTest.bills.size() == 0) {
                    result.add("Missing bill on order " + order.name + " for billable order.");
                }
            }
            if (!order.status.equals(OrderStatus.RECEIVED.getId())
                    && !order.status.equals(OrderStatus.FILLED.getId())
                    && !order.status.equals(OrderStatus.PRODUCTION.getId())
                    && !order.status.equals(OrderStatus.BILLABLE.getId())) {
                if (!orderTest.bills.stream().anyMatch(b -> b.creditnotes.size() == 0)) {
                    result.add("Missing bill for honored order.");
                }
            }
            if (!order.status.equals(OrderStatus.RECEIVED.getId())
                    && !order.status.equals(OrderStatus.FILLED.getId())
                    && !order.status.equals(OrderStatus.PRODUCTION.getId())
                    && !order.status.equals(OrderStatus.BILLABLE.getId())
                    && !order.status.equals(OrderStatus.HONORED.getId())) {
                Facture facture = orderTest.bills.stream().filter(b -> b.creditnotes.size() == 0).findFirst().get();
                BigDecimal total = facture.paiements.stream().map(p -> p.value).reduce(BigDecimal.ZERO, BigDecimal::add);
                if (total.subtract(facture.lignes.stream().map(l -> l.total).reduce(BigDecimal.ZERO, BigDecimal::add)).compareTo(BigDecimal.ZERO) != 0) {
                    result.add("Payments not done for closed order (value : " + total.toString() + ".");
                }
            }
        }
        if (order.status.equals(OrderStatus.RECEIVED.getId()) || order.status.equals(OrderStatus.FILLED.getId()) || order.status.equals(OrderStatus.PRODUCTION.getId())) {
            if (orderTest.bills.size() != 0) {
                result.add("Bill for order " + order.name + " with status " + order.status + ".");
            }
        }
        if (orderTest.prestations.stream().filter(p -> p.prestation.estate.isPresent()).collect(Collectors.toList()).size() != 0 && !orderTest.estate.isPresent()) {
            result.add("Missing estate for order.");
        }

        for (Facture bill : orderTest.bills) {
            if (bill.status.equals(BillStatus.UNKNOWN.getId())) {
                result.add("Wrong status " + bill.status + " for bill " + bill.uuid + ".");
            } else if (bill.status.equals(BillStatus.CANCELLED.getId()) && bill.creditnotes.size() == 0) {
                result.add("Cancelled bill " + bill.status + " without credit note for bill " + bill.uuid + ".");
            } else if (!bill.status.equals(BillStatus.CANCELLED.getId()) && bill.creditnotes.size() > 0) {
                result.add("Status " + bill.status + " with credit note for bill " + bill.uuid + ".");
            } else {
                if (!bill.status.equals(BillStatus.PENDING.getId())) {
                    if (bill.lignes.size() == 0) {
                        result.add("No line for confirmed bill " + bill.uuid + ".");
                    }
                    if (!order.status.equals(OrderStatus.HONORED.getId())) {
                        result.add("Bill confirmed but order status is not honored");
                    }
                }
                if (!bill.status.equals(BillStatus.PENDING.getId())
                        && !bill.status.equals(BillStatus.CONFIRMED.getId())) {
                    if (!bill.exportDate.isPresent()) {
                        result.add("No export date for bill " + bill.uuid + " with status billed.");
                    }
                }
            }
        }
        List<Facture> billsWithoutCreditNote = orderTest.bills.stream().filter(b -> b.creditnotes.size() == 0).collect(Collectors.toList());
        if (billsWithoutCreditNote.size() > 1) {
            result.add("More than one uncancelled bill present.");
        } else if (billsWithoutCreditNote.size() > 0) {
            Facture finalBill = billsWithoutCreditNote.stream().findFirst().get();
            if (!finalBill.status.equals(BillStatus.PENDING.getId())) {
                for (PrestationWithResult prestationWithResult : orderTest.prestations) {
                    if (!prestationWithResult.prestation.orderLine.isPresent()) {
                        result.add("Missing bill line on prestation " + prestationWithResult.prestation.uuid + " despite validate bill " + finalBill.uuid + ".");
                    }
                }
            }
        }

        return result;
    }

    @Inject
    public MainController(final AccountsService accountsService, EstablishmentsService establishmentsService,
                          final OrdersService ordersService, final BillsService billsService, final MissionClient missionClient,
                          final EstateClient estateClient, final FileTransfer fileTransfer, final Sage1000ExportUtils sage1000ExportUtils,
                          final UserSynchronization userSynchroniztion, Config config) {
        this.accountsService = accountsService;
        this.establishmentsService = establishmentsService;
        this.ordersService = ordersService;
        this.billsService = billsService;
        this.missionClient = missionClient;
        this.estateClient = estateClient;
        this.fileTransfer = fileTransfer;
        this.sage1000ExportUtils = sage1000ExportUtils;
        this.userSynchroniztion = userSynchroniztion;
        this.config = config;
    }

    public CompletionStage<Result> status() {
        return CompletableFuture.completedFuture(ok());
    }

    public CompletionStage<Result> testOrder(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        return ordersService.getAll(organization).thenCompose(orders -> {
            List<CompletableFuture<Optional<OrderTest>>> testResults = orders.stream().map(order -> get(organization, order).toCompletableFuture()).collect(Collectors.toList());
            if (entries.containsKey("clear") && entries.get("clear")[0].equalsIgnoreCase("true")) {
                return CompletableFutureUtils.sequence(testResults).thenCompose(res -> CompletableFutureUtils.sequence(res.stream().filter(o -> o.isPresent()).map(orderTest -> {
                    List<String> errors = check(orderTest.get());
                    if (errors.size() != 0) {
                        return CompletableFutureUtils.sequence(orderTest.get().bills.stream().map(b -> billsService.delete(organization, b.uuid).toCompletableFuture()).collect(Collectors.toList())).thenCompose(done -> ordersService.delete(organization, orderTest.get().order.uuid).thenApply(d -> "Order " + orderTest.get().order.uuid + "/" + orderTest.get().order.name + " has been deleted (don\'t forget to remove interventions) !").toCompletableFuture());
                    } else {
                        return CompletableFuture.completedFuture("Order " + orderTest.get().order.uuid + "/" + orderTest.get().order.name + " is clean !");
                    }
                }).collect(Collectors.toList())).thenApply(messageList -> ok(Json.toJson(messageList))));
            } else {
                return CompletableFutureUtils.sequence(testResults).thenApply(res -> ok(Json.toJson(res.stream().filter(o -> o.isPresent()).map(orderTest -> {
                    if (entries.containsKey("type")) {
                        if (entries.get("type")[0].equals("full")) {
                            return orderTest;
                        } else {
                            return new TestResult(orderTest.get().order.uuid, orderTest.get().order.name, check(orderTest.get()));
                        }
                    } else {
                        return new TestResult(orderTest.get().order.uuid, orderTest.get().order.name, check(orderTest.get()));
                    }
                }))));
            }
        });
    }

    public CompletionStage<Result> getFiles(String organization) {
        Optional<List<String>> files = fileTransfer.listFiles();
        if (files.isPresent()) {
            return CompletableFuture.completedFuture(ok(Json.toJson(files.get())));
        } else {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when listing files in ftps server."))));
        }
    }

    public CompletionStage<Result> testPaymentFile(Http.Request request, String organization) {
        try {
            File file = request.body().asRaw().asFile();
            List<List<String>> records = new ArrayList<>();
            CSVParserBuilder parser = new CSVParserBuilder().withSeparator(';');
            CSVReader csvReader = new CSVReaderBuilder(new FileReader(file))
                    .withSkipLines(1)
                    .withCSVParser(parser.build())
                    .build();
            String[] values;
            while ((values = csvReader.readNext()) != null) {
                records.add(Arrays.asList(values));
            }
            Map<String, List<Payment>> payments = PaymentParsingFile.parsePaymentFile(records);
            return CompletableFutureUtils.sequence(payments.keySet().stream().map(billName -> billsService.getFromName(organization, billName).thenCompose(bill -> {
                if (bill.isPresent()) {
                    return CompletableFutureUtils.sequence(payments.get(billName).stream().map(payment -> billsService.addPaiement(organization, payment, bill.get()).toCompletableFuture()).collect(Collectors.toList()));
                } else {
                    logger.error("Wrong bill from payment file : " + billName);
                    return CompletableFuture.completedFuture(new ArrayList<>());
                }
            }).toCompletableFuture()).collect(Collectors.toList()))
                    .thenApply(results -> {
                        if (results.stream().allMatch(r -> r.size() > 0 && r.stream().allMatch(Optional::isPresent))) {
                            return ok(Json.toJson(new SuccessMessage("All payments have been added !")));
                        } else {
                            return internalServerError(Json.toJson(new ErrorMessage("Error during payment process.")));
                        }
                    });
        } catch (FileNotFoundException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("File not found on body."))));
        } catch (IOException | CsvValidationException e) {
            logger.error("Error while parsing csv: ", e);
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Error while parsing csv"))));
        }
    }

    public CompletionStage<Result> transfer(Http.Request request, String organization) {
        try {
            final Map<String, String[]> entries = request.queryString();
            File file = request.body().asRaw().asFile();
            InputStream inputStream = new FileInputStream(file);
            String fileName = entries.containsKey("filename") ? entries.get("filename")[0] : "default_name.txt";
            Boolean fileTransfered = fileTransfer.sendFile(fileName, inputStream, false);
            if (fileTransfered) {
                return CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("File has been transferred !"))));
            } else {
                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error during transfer of file " + fileName))));
            }
        } catch (FileNotFoundException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("File not found on body."))));
        }
    }

    public CompletionStage<Result> getFile(Http.Request request, String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("filepath")) {
            final String fileName = entries.get("filepath")[0];
            Optional<byte[]> byteArray = fileTransfer.getSingleFile(fileName);
            if (byteArray.isPresent()) {
                String[] split = fileName.split("/");
                return CompletableFuture.completedFuture(ok().sendBytes(byteArray.get()).withHeader("Content-Disposition", "attachment; filename=\"" + split[split.length - 1] + "\""));
            } else {
                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when getting " + fileName + "."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Filepath required."))));
        }
    }

    public CompletionStage<Result> renameFile(Http.Request request, String organization) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("old") && entries.containsKey("new")) {
            final String oldFilePath = entries.get("old")[0];
            final String newFilePath = entries.get("new")[0];
            Boolean renamed = fileTransfer.renameFile(oldFilePath, newFilePath);
            if (renamed) {
                return CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("Renaming succesful !"))));
            } else {
                return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when renaming " + oldFilePath + " to " + newFilePath + "."))));
            }
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Filepath required."))));
        }
    }

    public CompletionStage<Result> ldapTrigger(String organization) {
        return userSynchroniztion.synchronizeUser().thenApply(done -> {
            if (done) {
                return ok(Json.toJson(new SuccessMessage("LDAP sync succeded.")));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Error during LDAP sync.")));
            }
        });
    }

    public CompletionStage<Result> sage1000Trigger(String organization) {
        return sage1000ExportUtils.export(organization).thenCompose(exportDone ->
                sage1000ExportUtils.parse(organization).thenApply(parsingDone -> {
                    if (!exportDone && !parsingDone) {
                        return internalServerError(Json.toJson(new ErrorMessage("Error during export and import with Sage1000 server.")));
                    } else if (!exportDone) {
                        return internalServerError(Json.toJson(new ErrorMessage("Error during export to Sage1000 server.")));
                    } else if (!parsingDone) {
                        return internalServerError(Json.toJson(new ErrorMessage("Error during parsing from Sage1000 server.")));
                    } else {
                        return ok(Json.toJson(new SuccessMessage("Export and parsing with Sage1000 server succeded.")));
                    }
                }));
    }
}
