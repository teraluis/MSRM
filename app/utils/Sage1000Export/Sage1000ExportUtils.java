package utils.Sage1000Export;

import accounts.AccountsService;
import api.v1.models.*;
import bills.BillStatus;
import bills.BillsService;
import bills.Payment;
import core.CompletableFutureUtils;
import core.models.Prestation;
import establishments.EstablishmentsService;
import estateWithAddress.EstateWithAddress;
import missionclient.interventions.IncompleteIntervention;
import missionclient.interventions.MaterializedIntervention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Tuple2;
import scala.Tuple6;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sage1000ExportUtils {

    private final FileTransfer fileTransfer;
    private final EstablishmentsService establishmentsService;
    private final AccountsService accountsService;
    private final BillsService billsService;
    protected final Logger logger = LoggerFactory.getLogger(Sage1000ExportUtils.class);
    private final DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    private Boolean sendToServer(String fileName, List<String> lines, String firstLine) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(firstLine.getBytes(StandardCharsets.ISO_8859_1));
            for (String line : lines) {
                outputStream.write(line.getBytes(StandardCharsets.ISO_8859_1));
            }
            byte[] updateByteArray = outputStream.toByteArray();
            InputStream inputStream = new ByteArrayInputStream(updateByteArray);
            return fileTransfer.sendFile(fileName, inputStream, true);
        } catch (Exception e) {
            logger.error("Error when trying to send file to server.");
            return false;
        }
    }

    @Inject
    public Sage1000ExportUtils(FileTransfer fileTransfer, EstablishmentsService establishmentsService, AccountsService accountsService, BillsService billsService) {
        this.fileTransfer = fileTransfer;
        this.establishmentsService = establishmentsService;
        this.accountsService = accountsService;
        this.billsService = billsService;
    }


    private CompletionStage<Boolean> getValidatorsExport(String organization) {
        return establishmentsService.getAll(organization).thenCompose(establishments -> this.accountsService.getAdministrativeValidatorsForExport(organization).thenCompose(accounts -> CompletableFutureUtils.sequence(establishments.stream().filter(e -> !e.validatorExported).map(establishment -> establishmentsService.serializeFull(organization, establishment, false).toCompletableFuture()).collect(Collectors.toList())).thenApply(fullEstablishments -> fullEstablishments.stream().filter(e -> accounts.stream().map(a -> a.uuid).collect(Collectors.toList()).contains(e.account.uuid)).map(fullEstablishment -> new Tuple2<>(fullEstablishment, new Sage1000ClientCsvLine(Sage1000ClientCsvLine.generateValues(fullEstablishment, true)).generateLine(Sage1000ClientCsvUtils.getColumns()))).collect(Collectors.toList()))
                .thenCompose(tuples -> {
                    List<String> lines = tuples.stream().map(t -> t._2).collect(Collectors.toList());
                    if (lines.size() > 0) {
                        String firstLine = Stream.of(Sage1000ClientCsvUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                        // if (sendToMinio(minioClient, bucketName, "validators", lines, firstLine)) {
                        if (sendToServer("CAL_VALIDEURS_" + dateFormat.format(new Date()) + ".txt", lines, firstLine)) {
                            return CompletableFutureUtils.sequence(tuples.stream().map(t -> establishmentsService.setValidatorExported(organization, t._1.establishment.uuid).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(done -> done.stream().allMatch(t -> t));
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                })));
    }

    private CompletionStage<Boolean> getUpdatedValidatorsExport(String organization) {
        return establishmentsService.getAll(organization).thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().filter(e -> e.validatorExported).map(establishment -> establishmentsService.serializeFull(organization, establishment, false).toCompletableFuture()).collect(Collectors.toList())).thenApply(fullEstablishments -> fullEstablishments.stream().map(fullEstablishment -> new Tuple2<>(fullEstablishment, new Sage1000ClientCsvLine(Sage1000ClientCsvLine.generateValues(fullEstablishment, true)).generateLine(Sage1000ClientCsvUtils.getColumns()))).collect(Collectors.toList()))
                .thenCompose(tuples -> {
                    List<String> lines = tuples.stream().map(t -> t._2).collect(Collectors.toList());
                    if (lines.size() > 0) {
                        String firstLine = Stream.of(Sage1000ClientCsvUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                        // if (sendToMinio(minioClient, bucketName, "updatedValidators", lines, firstLine)) {
                        if (sendToServer("UPDATE_CAL_VALIDEURS_" + dateFormat.format(new Date()) + ".txt", lines, firstLine)) {
                            return CompletableFutureUtils.sequence(tuples.stream().map(t -> establishmentsService.setValidatorUpToDate(organization, t._1.establishment.uuid).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(done -> done.stream().allMatch(t -> t));
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                }));
    }

    private CompletionStage<Boolean> getClientsExport(String organization) {
        return establishmentsService.getAll(organization).thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().filter(e -> !e.clientExported).map(establishment -> establishmentsService.serializeFull(organization, establishment, false).toCompletableFuture()).collect(Collectors.toList())).thenApply(fullEstablishments -> fullEstablishments.stream().filter(e -> e.account.category.equals("Client")).map(fullEstablishment -> new Sage1000ClientCsvLine(Sage1000ClientCsvLine.generateValues(fullEstablishment, false)).generateLine(Sage1000ClientCsvUtils.getColumns())).collect(Collectors.toList()))
                .thenCompose(lines -> {
                    if (lines.size() > 0) {
                        String firstLine = Stream.of(Sage1000ClientCsvUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                        // if (sendToMinio(minioClient, bucketName, "tuple._2().establishment.get().establishments", lines, firstLine)) {
                        if (sendToServer("CAL_TIERS_" + dateFormat.format(new Date()) + ".txt", lines, firstLine)) {
                            return CompletableFutureUtils.sequence(establishments.stream().map(e -> establishmentsService.setClientExported(organization, e.uuid).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(done -> done.stream().allMatch(t -> t));
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                }));
    }

    private CompletionStage<Boolean> getUpdatedClientsExport(String organization) {
        return establishmentsService.getAll(organization).thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().filter(e -> e.clientModified).map(establishment -> establishmentsService.serializeFull(organization, establishment, false).toCompletableFuture()).collect(Collectors.toList())).thenApply(fullEstablishments -> fullEstablishments.stream().map(fullEstablishment -> new Sage1000ClientCsvLine(Sage1000ClientCsvLine.generateValues(fullEstablishment, false)).generateLine(Sage1000ClientCsvUtils.getColumns())).collect(Collectors.toList()))
                .thenCompose(lines -> {
                    if (lines.size() > 0) {
                        String firstLine = Stream.of(Sage1000ClientCsvUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                        // if (sendToMinio(minioClient, bucketName, "updatedClients", lines, firstLine)) {
                        if (sendToServer("UPDATE_CAL_TIERS_" + dateFormat.format(new Date()) + ".txt", lines, firstLine)) {
                            return CompletableFutureUtils.sequence(establishments.stream().map(e -> establishmentsService.setClientUpToDate(organization, e.uuid).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(done -> done.stream().allMatch(t -> t));
                        } else {
                            return CompletableFuture.completedFuture(false);
                        }
                    } else {
                        return CompletableFuture.completedFuture(true);
                    }
                }));
    }

    private CompletionStage<List<String>> getBillExport(String organization) {
        return billsService.getBillsToExport(organization).thenCompose(tuples -> {
            List<Tuple6<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency>> finalTuples = tuples.stream().filter(tuple -> tuple._2().establishment.isPresent()).filter(tuple -> tuple._2().market.isPresent()).collect(Collectors.toList());
            List<String> billLines = new ArrayList<>();
            for (Tuple6<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency> tuple : finalTuples) {
                List<Prestation> prestations = tuple._4().stream().map(i -> ((IncompleteIntervention) i).getPrestations()).flatMap(List::stream).collect(Collectors.toList());
                Facture bill = tuple._1();
                Order order = tuple._2();
                Agency commercialAgency = tuple._6();
                Optional<Agency> productionAgency = order.agency;
                Boolean differentAgencies = productionAgency.isPresent() && !productionAgency.get().uuid.equals(commercialAgency.uuid);
                // TODO : handle order for individual
                List<Tuple2<Integer, String>> TVAinfos = prestations.stream().filter(p -> p.technicalAct.isPresent()).map(p -> new Tuple2<>(p.technicalAct.get().typeTVA, p.technicalAct.get().codeTVA)).distinct().collect(Collectors.toList());
                if (!bill.exportDate.isPresent()) {
                    // TODO : get the real order.establishment.get().establishment, not the first from marketAccounts and handle order.establishment.get().establishments not from markets
                    String line = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000BillLine(bill.name, bill.lignes, order.establishment.get().establishment, order.billedEstablishment, false, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns());
                    billLines.add(line);
                    for (Tuple2<Integer, String> TVAtuple : TVAinfos) {
                        if (!TVAtuple._2.equals("")) {
                            String tvaLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000TaxLine(bill.name, bill.lignes.stream().map(l -> {
                                BigDecimal priceWithoutTaxNorDiscount = l.price.multiply(new BigDecimal(l.quantity));
                                BigDecimal priceWithoutTax = priceWithoutTaxNorDiscount.subtract(priceWithoutTaxNorDiscount.multiply(l.discount.divide(new BigDecimal(100))));
                                return priceWithoutTax.multiply(new BigDecimal(TVAtuple._1).divide(new BigDecimal(100)));
                            }).reduce(BigDecimal.ZERO, BigDecimal::add), order.establishment.get().establishment, false, TVAtuple._2, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns());
                            billLines.add(tvaLine);
                        }
                    }
                    for (Tuple2<Integer, String> TVAtuple : TVAinfos) {
                        List<String> codeTVABillLines = prestations.stream().filter(p -> p.technicalAct.isPresent() && p.technicalAct.get().codeTVA.equals(TVAtuple._2)).map(p -> p.billLines).flatMap(List::stream).collect(Collectors.toList());
                        List<FactureLigne> prestationLines = bill.lignes.stream().filter(l -> codeTVABillLines.contains(l.uuid) && !l.refadx.toUpperCase().equals("ANALYSE")).collect(Collectors.toList());
                        String profilTVA = prestations.stream().filter(p -> p.technicalAct.isPresent() && p.technicalAct.get().codeTVA.equals(TVAtuple._2)).findAny().get().technicalAct.get().profilTVA;
                        String prestationLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000PrestationLine(bill.name, "70610000", prestationLines, order.establishment.get().establishment, false, profilTVA, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns());
                        billLines.add(prestationLine);
                        List<String> expertCodesFromPrestations = prestationLines.stream().map(p -> p.expertCode).distinct().collect(Collectors.toList());
                        for (String expertCode : expertCodesFromPrestations) {
                            List<FactureLigne> axeLines = prestationLines.stream().filter(l -> l.expertCode.equals(expertCode)).collect(Collectors.toList());
                            if (!differentAgencies) {
                                String axeLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, expertCode, false, order.commercial.get(), new BigDecimal(1))).generateLine(Sage1000BillCsvUtils.getColumns());
                                billLines.add(axeLine);
                            } else {
                                String productionLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70610000", axeLines, order.establishment.get().establishment, productionAgency.get().code, expertCode, false, order.commercial.get(), new BigDecimal(0.95))).generateLine(Sage1000BillCsvUtils.getColumns());
                                billLines.add(productionLine);
                                String commercialLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, "9999", false, order.commercial.get(), new BigDecimal(0.05))).generateLine(Sage1000BillCsvUtils.getColumns());
                                billLines.add(commercialLine);
                            }
                        }
                        List<FactureLigne> analyseLines = bill.lignes.stream().filter(l -> codeTVABillLines.contains(l.uuid) && l.refadx.toUpperCase().equals("ANALYSE")).collect(Collectors.toList());
                        if (analyseLines.size() > 0) {
                            String analyseLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000PrestationLine(bill.name, "70620000", analyseLines, order.establishment.get().establishment, false, profilTVA, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns());
                            billLines.add(analyseLine);
                            List<String> expertCodesFromAnalysis = analyseLines.stream().map(p -> p.expertCode).distinct().collect(Collectors.toList());
                            for (String expertCode : expertCodesFromAnalysis) {
                                List<FactureLigne> axeLines = analyseLines.stream().filter(l -> l.expertCode.equals(expertCode)).collect(Collectors.toList());
                                if (!differentAgencies) {
                                    String axeLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70620000", axeLines, order.establishment.get().establishment, commercialAgency.code, expertCode, false, order.commercial.get(), new BigDecimal(1))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(axeLine);
                                } else {
                                    String productionLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70610000", axeLines, order.establishment.get().establishment, productionAgency.get().code, expertCode, false, order.commercial.get(), new BigDecimal(0.95))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(productionLine);
                                    String commercialLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(bill.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, "9999", false, order.commercial.get(), new BigDecimal(0.05))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(commercialLine);
                                }
                            }
                        }
                    }
                }
                // TODO : use-cases when expert is not ADX internal
                for (Avoir creditNote : bill.creditnotes) {
                    if (!creditNote.exportdate.isPresent()) {
                        // TODO : get the real client, not the first from marketAccounts
                        billLines.add(new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000BillLine(creditNote.name, creditNote.lignes, order.establishment.get().establishment, order.billedEstablishment, true, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns()));
                        for (Tuple2<Integer, String> TVAtuple : TVAinfos) {
                            billLines.add(new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000TaxLine(creditNote.name, creditNote.lignes.stream().map(l -> {
                                BigDecimal priceWithoutTaxNorDiscount = l.price.multiply(new BigDecimal(l.quantity));
                                BigDecimal priceWithoutTax = priceWithoutTaxNorDiscount.subtract(priceWithoutTaxNorDiscount.multiply(l.discount.divide(new BigDecimal(100))));
                                return priceWithoutTax.multiply(new BigDecimal(TVAtuple._1).divide(new BigDecimal(100)));
                            }).reduce(BigDecimal.ZERO, BigDecimal::add), order.establishment.get().establishment, true, TVAtuple._2, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns()));
                        }
                        for (Tuple2<Integer, String> TVAtuple : TVAinfos) {
                            List<String> codeTVABillLines = prestations.stream().filter(p -> p.technicalAct.isPresent() && p.technicalAct.get().codeTVA.equals(TVAtuple._2)).map(p -> p.billLines).flatMap(List::stream).collect(Collectors.toList());
                            String profilTVA = prestations.stream().filter(p -> p.technicalAct.isPresent() && p.technicalAct.get().codeTVA.equals(TVAtuple._2)).findAny().get().technicalAct.get().profilTVA;
                            List<FactureLigne> creditNotePrestationLines = bill.lignes.stream().filter(l -> codeTVABillLines.contains(l.uuid) && !l.refadx.toUpperCase().equals("ANALYSE")).collect(Collectors.toList());
                            billLines.add(new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000PrestationLine(creditNote.name, "70610000", creditNotePrestationLines, order.establishment.get().establishment, true, profilTVA, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns()));
                            List<String> codesFromPrestations = creditNotePrestationLines.stream().map(p -> p.expertCode).distinct().collect(Collectors.toList());
                            for (String expertCode : codesFromPrestations) {
                                List<FactureLigne> axeLines = creditNotePrestationLines.stream().filter(l -> l.expertCode.equals(expertCode)).collect(Collectors.toList());
                                if (!differentAgencies) {
                                    String axeLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, expertCode, true, order.commercial.get(), new BigDecimal(1))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(axeLine);
                                } else {
                                    String productionLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70610000", axeLines, order.establishment.get().establishment, productionAgency.get().code, expertCode, true, order.commercial.get(), new BigDecimal(0.95))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(productionLine);
                                    String commercialLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, "9999", true, order.commercial.get(), new BigDecimal(0.05))).generateLine(Sage1000BillCsvUtils.getColumns());
                                    billLines.add(commercialLine);
                                }
                            }
                            List<FactureLigne> creditNoteAnalyseLines = bill.lignes.stream().filter(l -> codeTVABillLines.contains(l.uuid) && l.refadx.toUpperCase().equals("ANALYSE")).collect(Collectors.toList());
                            if (creditNoteAnalyseLines.size() > 0) {
                                billLines.add(new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000PrestationLine(creditNote.name, "70620000", creditNoteAnalyseLines, order.establishment.get().establishment, true, profilTVA, order.commercial.get())).generateLine(Sage1000BillCsvUtils.getColumns()));
                                List<String> codesFromAnalysis = creditNoteAnalyseLines.stream().map(p -> p.expertCode).distinct().collect(Collectors.toList());
                                for (String expertCode : codesFromAnalysis) {
                                    List<FactureLigne> axeLines = creditNoteAnalyseLines.stream().filter(l -> l.expertCode.equals(expertCode)).collect(Collectors.toList());
                                    if (!differentAgencies) {
                                        String axeLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70620000", axeLines, order.establishment.get().establishment, commercialAgency.code, expertCode, true, order.commercial.get(), new BigDecimal(1))).generateLine(Sage1000BillCsvUtils.getColumns());
                                        billLines.add(axeLine);
                                    } else {
                                        String productionLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70610000", axeLines, order.establishment.get().establishment, productionAgency.get().code, expertCode, true, order.commercial.get(), new BigDecimal(0.95))).generateLine(Sage1000BillCsvUtils.getColumns());
                                        billLines.add(productionLine);
                                        String commercialLine = new Sage1000BillCsvLine(Sage1000BillCsvLine.generateSage1000AxeLine(creditNote.name, "70610000", axeLines, order.establishment.get().establishment, commercialAgency.code, "9999", true, order.commercial.get(), new BigDecimal(0.05))).generateLine(Sage1000BillCsvUtils.getColumns());
                                        billLines.add(commercialLine);
                                    }
                                }
                            }
                        }
                    }
                    // TODO : other lines with intervention
                }
            }
            return billsService.setExported(organization, new Date()).thenApply(setExported -> {
                if (setExported) {
                    return billLines;
                } else {
                    return new ArrayList<>();
                }
            });
        });
    }

    public CompletionStage<Boolean> export(String organization) {
        return getValidatorsExport(organization).thenCompose(validatorsDone -> {
            if (validatorsDone) {
                return getUpdatedValidatorsExport(organization).thenCompose(updatedValidatorsDone -> {
                    if (updatedValidatorsDone) {
                        return getClientsExport(organization).thenCompose(exportDone -> {
                            if (exportDone) {
                                return getUpdatedClientsExport(organization).thenCompose(updatedDone -> {
                                    if (updatedDone) {
                                        return getBillExport(organization).thenApply(billines -> {
                                            try {
                                                ByteArrayOutputStream billByteArrayOutputStream = new ByteArrayOutputStream();
                                                String firstLine = Stream.of(Sage1000BillCsvUtils.getColumns()).collect(Collectors.joining("\t")) + "\n";
                                                billByteArrayOutputStream.write(firstLine.getBytes(StandardCharsets.ISO_8859_1));
                                                for (String line : billines) {
                                                    billByteArrayOutputStream.write(line.getBytes(StandardCharsets.ISO_8859_1));
                                                }
                                                byte[] billByteArray = billByteArrayOutputStream.toByteArray();
                                                InputStream billInputStream = new ByteArrayInputStream(billByteArray);
                                                // minioClient.putObject(bucketName, "test/Sage1000/billExport.txt", billInputStream, new Long(billByteArray.length), null, null, "application/octet-stream");
                                                fileTransfer.sendFile("CAL_ECRITURES_" + dateFormat.format(new Date()) + ".txt", billInputStream, true);
                                                return true;
                                            } catch (Exception e) {
                                                logger.error(e.getMessage());
                                                return false;
                                            }
                                        });
                                    } else {
                                        logger.error("Error during establishments export.");
                                        return CompletableFuture.completedFuture(false);
                                    }
                                });
                            } else {
                                logger.error("Error during updated establishments export.");
                                return CompletableFuture.completedFuture(false);
                            }
                        });
                    } else {
                        logger.error("Error during validators export.");
                        return CompletableFuture.completedFuture(false);
                    }
                });
            } else {
                logger.error("Error during updated validators export.");
                return CompletableFuture.completedFuture(false);
            }
        });
    }

    public CompletionStage<Boolean> parse(String organization) {
        try {
            Optional<Tuple2<List<List<String>>, List<List<String>>>> tuple = fileTransfer.getFiles();
            if (tuple.isPresent()) {
                Map<String, List<Payment>> payments = PaymentParsingFile.parsePaymentFile(tuple.get()._1);
                return CompletableFutureUtils.sequence(payments.keySet().stream().map(billName -> billsService.getFromName(organization, billName).thenCompose(bill -> {
                    if (bill.isPresent()) {
                        return CompletableFutureUtils.sequence(payments.get(billName).stream().map(payment -> billsService.addPaiement(organization, payment, bill.get()).toCompletableFuture()).collect(Collectors.toList()));
                    } else {
                        logger.error("Wrong bill from payment file : {}", billName);
                        return CompletableFuture.completedFuture(new ArrayList<>());
                    }
                }).toCompletableFuture()).collect(Collectors.toList()))
                        .thenApply(results -> results.stream().allMatch(r -> !r.isEmpty() && r.stream().allMatch(Optional::isPresent)))
                        .thenCompose(paymentDone -> {
                            if (!paymentDone) {
                                logger.error("Error when adding payment in database.");
                            }
                            List<String> billNames = IrrecoverableParsingFile.parseIrrecoverableFile(tuple.get()._2);
                            return CompletableFutureUtils.sequence(billNames.stream().map(b -> billsService.getFromName(organization, b).thenCompose(bill -> {
                                if (bill.isPresent()) {
                                    return billsService.setStatus(organization, bill.get().uuid, BillStatus.RECOVERY, Optional.empty());
                                } else {
                                    logger.error("Wrong bill from irrecoveable file : {}", b);
                                    return CompletableFuture.completedFuture(false);
                                }
                            }).toCompletableFuture()).collect(Collectors.toList()))
                                    .thenApply(results -> {
                                        if (results.stream().allMatch(r -> r)) {
                                            return paymentDone;
                                        } else {
                                            logger.error("Error when changing status of bill in database.");
                                            return false;
                                        }
                                    });
                        });
            } else {
                return CompletableFuture.completedFuture(false);
            }
        } catch (Exception e) {
            logger.error("Error when parsing files from Sage1000.", e);
            return CompletableFuture.completedFuture(false);
        }
    }
}
