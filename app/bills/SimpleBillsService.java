package bills;

import accounts.AccountsService;
import addresses.AddressesService;
import agencies.AgenciesService;
import api.v1.models.*;
import com.typesafe.config.Config;
import core.CompletableFutureUtils;
import core.EventType;
import core.models.Prestation;
import core.models.TechnicalAct;
import core.search.AbstractSearchService;
import core.search.Pageable;
import core.search.PaginatedResult;
import core.search.SearchService;
import estateWithAddress.EstateWithAddress;
import estateWithAddress.LocalityWithAddress;
import estateclient.Estate;
import estateclient.EstateClient;
import estimates.EstimatesService;
import missionclient.Asbestos;
import missionclient.MissionClient;
import missionclient.interventions.DoneIntervention;
import missionclient.interventions.DraftIntervention;
import missionclient.interventions.MaterializedIntervention;
import orders.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdf.pdfdocument.AbstractPdfDocument;
import pdf.pdfdocument.BasicBillDocument;
import pdf.pdfdocument.File;
import play.libs.ws.WSClient;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple6;
import services.FileService;
import users.UsersService;

import javax.inject.Inject;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SimpleBillsService extends AbstractSearchService<IndexableBill> implements BillsService {
    private final Logger log = LoggerFactory.getLogger(SimpleBillsService.class);
    private final String ELASTIC_OBJECT_TYPE = "bill";

    protected final BillsRepository billsRepository;
    protected final OrdersService ordersService;
    protected final UsersService usersService;
    protected final AccountsService accountsService;
    protected final EstimatesService estimatesService;
    protected final AddressesService addressesService;
    protected final MissionClient missionClient;
    protected final EstateClient estateClient;
    protected final WSClient wsClient;
    protected final FileService fileService;
    protected final Config config;
    protected final SearchService searchService;
    protected final AgenciesService agenciesService;

    private CompletionStage<EstateWithAddress> getEstateWithAddressFromEstate(String organization, Estate estate) {
        return CompletableFutureUtils.sequence(estate.localities.stream().map(locality -> CompletableFutureUtils.sequence(locality.addresses.stream().map(a -> addressesService.get(organization, a).toCompletableFuture()).collect(Collectors.toList())).thenApply(optionalAddresses -> optionalAddresses.stream().filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList())).thenApply(addresses -> LocalityWithAddress.buildWithAddress(locality, addresses))).collect(Collectors.toList()))
                .thenApply(localityWithAddresses -> EstateWithAddress.buildEstateWithAddress(estate, localityWithAddresses));
    }

    private List<CompletableFuture<Optional<Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>>>>> getBillsDetails(String organization, List<api.v1.models.Facture> bills, List<TechnicalAct> prestationTypes) {
        TechnicalAct raat = prestationTypes.stream().filter(p -> p.shortcut.equalsIgnoreCase("RAAT")).findFirst().get();
        TechnicalAct dapp = prestationTypes.stream().filter(p -> p.shortcut.equalsIgnoreCase("DAPP")).findFirst().get();
        List<CompletableFuture<Optional<Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>>>>> results = bills.stream().map(b -> ordersService.get(organization, b.order)
                .thenCompose(order -> ordersService.serializeOrder(organization, order.get()))
                .thenCompose(finalOrder -> this.missionClient.getInterventionsFromOrder(organization, finalOrder.get().uuid, new String[]{"DONE"})
                        .thenCompose(interventions -> {
                            List<String> billLinesId = b.lignes.stream().map(l -> l.uuid).collect(Collectors.toList());
                            return CompletableFutureUtils.sequence(interventions.stream().filter(i -> {
                                List<String> interventionsInBill = ((DoneIntervention) i).getPrestations().stream().filter(p -> p.billLines.stream().anyMatch(billLine -> billLinesId.contains(billLine))).map(p -> p.mission).flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty)).collect(Collectors.toList());
                                return interventionsInBill.contains(i.getId());
                            }).map(intervention -> this.estateClient.getEstate(organization, ((DoneIntervention) intervention).getPrestations().stream().findFirst().get().estate.get()).thenCompose(estate -> getEstateWithAddressFromEstate(organization, estate.get())
                                    .thenCompose(estateWithAddress -> CompletableFutureUtils.sequence(((DoneIntervention) intervention).getPrestations().stream().filter(p -> (p.technicalAct.get().equals(raat.uuid) || p.technicalAct.get().equals(dapp.uuid)) && p.resultId.isPresent()).map(p -> this.missionClient.getAsbestosResult(p.resultId.get()).thenApply(a -> a.get()).toCompletableFuture()).collect(Collectors.toList()))
                                            .thenApply(asbestos -> new Tuple3<>(intervention, estateWithAddress, asbestos)))).toCompletableFuture()).collect(Collectors.toList()));
                        }).thenApply(tuple3 -> Optional.of(new Tuple3<>(b, finalOrder.get(), tuple3)))).toCompletableFuture()).collect(Collectors.toList());
        return results;
    }

    private CompletionStage<Optional<Tuple6<api.v1.models.Facture, api.v1.models.Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency>>> finishBillsToExport(final String organization, final api.v1.models.Facture b, final api.v1.models.Order finalOrder, final api.v1.models.Account account) {
        return this.missionClient
                .getInterventionsFromOrder(organization, finalOrder.uuid, new String[]{"DONE"})
                .thenCompose(interventions -> CompletableFutureUtils.sequence(interventions.stream().map(intervention -> estateClient.getEstate(organization, ((DoneIntervention) intervention).getPrestations().stream().findFirst().get().estate.get()).thenCompose(estate -> getEstateWithAddressFromEstate(organization, estate.get())).toCompletableFuture()).collect(Collectors.toList()))
                        .thenCompose(estates -> {
                            return agenciesService.getFromOfficeName(organization, finalOrder.commercial.get().office.get()).thenCompose(agency -> agenciesService.serialize(organization, agency.get())
                                    .thenApply(finalAgency -> Optional.of(new Tuple6<>(b, finalOrder, account, interventions, estates, finalAgency))));
                        }));
    }

    private CompletionStage<FactureLigne> serializeLine(String organization, BillLine ligne, List<MaterializedIntervention> interventions) {
        return interventions.stream()
                .filter(intervention -> ((DoneIntervention) intervention).getPrestations().stream().anyMatch(p -> p.billLines.contains(ligne.uuid)))
                .findAny()
                .flatMap(intervention -> ((DoneIntervention) intervention)
                        .getPlanning()
                        .getExpert()
                        .pager.map(code -> usersService.getFromRegistrationNumber(organization, code)
                                .thenApply(user -> {
                                    if (user.isPresent()) {
                                        if (user.get().description.isPresent() && user.get().description.get().toLowerCase().contains("sous traitant")) {
                                            return FactureLigne.serialize(ligne, "0000");
                                        } else {
                                            return FactureLigne.serialize(ligne, code.substring(3, 7));
                                        }
                                    } else {
                                        return FactureLigne.serialize(ligne, "");
                                    }
                                })
                        )
                )
                .orElse(CompletableFuture.completedFuture(FactureLigne.serialize(ligne, "")));
    }

    private String getNewName(String prefix, Optional<String> lastName) {
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyMM");
        String dateFormat = format.format(date);
        if (lastName.isPresent() && !lastName.get().equals("")) {
            String substring = lastName.get().substring(1, 5);
            if (substring.equals(dateFormat)) {
                Integer number = Integer.parseInt(lastName.get().substring(5, 10));
                number = number + 1;
                String withZeros = String.format("%05d", number);
                return prefix + dateFormat + withZeros;
            } else {
                return prefix + dateFormat + "00001";
            }
        } else {
            return prefix + dateFormat + "00001";
        }
    }

    private CompletionStage<Optional<Bill>> updateBill(String organization, Bill bill, List<BillLine> lines) {
        return supplyAsync(() -> this.billsRepository.update(organization, bill))
                .thenCompose(newBill -> {
                    if (newBill.isPresent()) {
                        return CompletableFutureUtils.sequence(lines.stream().map(line -> supplyAsync(() -> this.billsRepository.addLine(newBill.get().uuid, line)).toCompletableFuture()).collect(Collectors.toList()));
                    } else {
                        return CompletableFuture.completedFuture(new ArrayList<>());
                    }
                }).thenCompose(resultArray ->
                        buildIndexableBill(organization, bill)
                                .thenCompose(indexableOrder -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableOrder))
                                .thenApply(res -> resultArray)
                ).thenApply(resultArray -> {
                    if (!resultArray.isEmpty() && resultArray.stream().allMatch(a -> a)) {
                        return Optional.of(bill);
                    } else {
                        return Optional.empty();
                    }
                });
    }


    @Inject()
    public SimpleBillsService(final BillsRepository billsRepository,
                              final OrdersService ordersService,
                              final UsersService usersService,
                              final AccountsService accountsService,
                              final EstimatesService estimatesService,
                              final AddressesService addressesService,
                              final MissionClient missionClient,
                              final EstateClient estateClient,
                              final WSClient wsClient,
                              final FileService fileService,
                              final Config config,
                              final SearchService searchService,
                              final AgenciesService agenciesService) {
        super(IndexableBill.class, searchService);
        this.billsRepository = billsRepository;
        this.ordersService = ordersService;
        this.usersService = usersService;
        this.accountsService = accountsService;
        this.estimatesService = estimatesService;
        this.addressesService = addressesService;
        this.missionClient = missionClient;
        this.estateClient = estateClient;
        this.wsClient = wsClient;
        this.fileService = fileService;
        this.config = config;
        this.searchService = searchService;
        this.agenciesService = agenciesService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Bill bill) {
        return supplyAsync(() -> this.billsRepository.add(organization, bill))
                .thenApply(idBill -> {
                    idBill.ifPresent(s -> this.addComment(organization, new BillComment(Optional.empty(), s, Optional.of("calypso"), "<b>Status :</b>Facture en attente", new Date(), EventType.STATUS)));
                    return idBill;
                }).thenCompose(idBill ->
                        buildIndexableBill(organization, bill)
                                .thenCompose(indexableOrder -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableOrder))
                                .thenApply(res -> idBill)
                );
    }

    @Override
    public CompletionStage<Optional<String>> addPaiement(String organization, Payment payment, Bill facture) {
        return supplyAsync(() -> this.billsRepository.addPaiement(payment, facture.uuid))
                .thenCompose(done -> {
                    if (done.isPresent()) {
                        return serialize(organization, facture, Optional.empty(), false)
                                .thenCompose(finalBill -> {
                                    BigDecimal totalExpected = finalBill.lignes.stream().map(l -> l.total).reduce(BigDecimal.ZERO, BigDecimal::add);
                                    BigDecimal totalPayment = finalBill.paiements.stream().map(p -> p.value).reduce(BigDecimal.ZERO, BigDecimal::add);
                                    if (totalPayment.compareTo(totalExpected) >= 0) {
                                        return this.setStatus(organization, facture.uuid, BillStatus.PAID, Optional.empty()).thenApply(changed -> {
                                            if (changed) {
                                                return done;
                                            } else {
                                                return Optional.empty();
                                            }
                                        });
                                    } else {
                                        return CompletableFuture.completedFuture(done);
                                    }
                                });
                    } else {
                        return CompletableFuture.completedFuture(done);
                    }
                });
    }

    /*
    @Override
    public CompletionStage<Boolean> generateNames(String organization) {
        return CompletableFuture.supplyAsync(() -> billsRepository.getLastBillName(organization)).thenCompose(lastBillName -> getAll(organization)
                .thenApply(bills -> {
                    List<Boolean> result = new ArrayList<>();
                    Optional<String> billName = lastBillName;
                    List<Bill> filteredBills = bills.stream().filter(b -> b.status.getId().equals(BillStatus.CONFIRMED.getId())).filter(b -> !b.exportdate.isPresent()).collect(Collectors.toList());
                    for (Bill b : filteredBills) {
                        billName = Optional.of(getNewName("B", billName));
                        result.add(billsRepository.setName(b, billName.get()));
                    }
                    return result.size() == filteredBills.size();
                }).thenCompose(done -> {
                    if (done) {
                        return CompletableFuture.supplyAsync(() -> billsRepository.getLastCreditNoteName(organization))
                                .thenCompose(lastCreditNoteName -> CompletableFuture.supplyAsync(() -> billsRepository.getAllCreditNotes(organization))
                                        .thenApply(creditNotes -> {
                                            List<Boolean> result = new ArrayList<>();
                                            Optional<String> creditNoteName = lastCreditNoteName;
                                            List<CreditNote> filteredCreditNotes = creditNotes.stream().filter(c -> !c.exportdate.isPresent()).collect(Collectors.toList());
                                            for (CreditNote creditNote : filteredCreditNotes) {
                                                creditNoteName = Optional.of(getNewName("V", creditNoteName));
                                                result.add(billsRepository.setName(creditNote, creditNoteName.get()));
                                            }
                                            return result.size() == filteredCreditNotes.size();
                                        }));
                    } else {
                        return CompletableFuture.completedFuture(false);
                    }
                }));
    }
     */

    @Override
    public CompletionStage<List<Bill>> getAll(String organization) {
        return supplyAsync(() -> this.billsRepository.getAll(organization));
    }

    public CompletionStage<List<Bill>> getPage(String organization, Integer offset, Integer length) {
        return supplyAsync(() -> this.billsRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Tuple6<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency>>> getBillsToExport(String organization) {
        // passer en défensif ? Ici je fais confiance aux status : si on a une facture que l'on peut exporter, on a forcément le reste.
        return CompletableFuture.supplyAsync(() -> this.billsRepository.getToExport(organization)).thenCompose(bills -> {
            List<CompletableFuture<Optional<Tuple6<Facture, api.v1.models.Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency>>>> results = bills.stream().map(b ->
                    serialize(organization, b, Optional.empty(), false).thenCompose(finalBill ->
                            this.ordersService.get(organization, b.order).thenCompose(order -> {
                                if (order.isPresent()) {
                                    return this.ordersService.serializeOrder(organization, order.get()).thenCompose(finalOrder -> {
                                        if (finalOrder.isPresent()) {
                                            if (finalOrder.get().market.isPresent()) {
                                                return accountsService.getFromEntity(organization, finalOrder.get().market.get().getDefaultEstablishment().get().entity).thenCompose(account -> {
                                                    if (account.isPresent()) {
                                                        return accountsService.serialize(organization, account.get()).thenCompose(finalAccount -> finishBillsToExport(organization, finalBill, finalOrder.get(), finalAccount));
                                                    } else {
                                                        return CompletableFuture.completedFuture(Optional.empty());
                                                    }
                                                });
                                            } else if (finalOrder.get().estimate.isPresent()) {
                                                if (finalOrder.get().estimate.get().account.isPresent()) {
                                                    return finishBillsToExport(organization, finalBill, finalOrder.get(), finalOrder.get().estimate.get().account.get());
                                                } else {
                                                    return CompletableFuture.completedFuture(Optional.empty());
                                                }
                                            } else {
                                                return CompletableFuture.completedFuture(Optional.empty());
                                            }
                                        } else {
                                            return CompletableFuture.completedFuture(Optional.empty());
                                        }
                                    });
                                } else {
                                    return CompletableFuture.completedFuture(Optional.empty());
                                }
                            })).toCompletableFuture()).collect(Collectors.toList());
            return CompletableFutureUtils.sequence(results).thenApply(tuple -> tuple.stream().filter(t -> t.isPresent()).map(t -> t.get()).collect(Collectors.toList()));
        });
    }

    @Override
    public CompletionStage<List<Tuple3<Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>>>> getExportedBills
            (String organization, Date startDate, Date endDate) {
        // passer en défensif ? Ici je fais confiance aux status : si on a une facture que l'on peut exporter, on a forcément le reste.
        return supplyAsync(() -> this.billsRepository.getExported(organization, startDate, endDate)).thenCompose(bills -> CompletableFutureUtils.sequence(bills.stream().map(bill -> serialize(organization, bill, Optional.of(new Tuple2<>(startDate, endDate)), false).toCompletableFuture()).collect(Collectors.toList()))).thenCompose(finalBills ->
                this.missionClient.listPrestationType().thenCompose(prestationTypes -> {
                    List<CompletableFuture<Optional<Tuple3<api.v1.models.Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>>>>> results = getBillsDetails(organization, finalBills, prestationTypes);
                    return CompletableFutureUtils.sequence(results).thenApply(r -> r.stream().filter(t -> t.isPresent()).map(t -> t.get()).collect(Collectors.toList()));
                })
        );
    }

    @Override
    public CompletionStage<List<Bill>> getFromOrder(String organization, String order) {
        return supplyAsync(() -> this.billsRepository.getFromOrder(organization, order));
    }

    @Override
    public CompletionStage<List<Bill>> search(String organization, String pattern) {
        return supplyAsync(() -> this.billsRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Bill>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return supplyAsync(() -> this.billsRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Boolean> setStatus(String organization, String uuid, BillStatus status, Optional<String> login) {
        return supplyAsync(() -> {
            final boolean setted = this.billsRepository.setStatus(organization, uuid, status);
            if (setted) {
                if (status.equals(BillStatus.BILLED)) {
                    this.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>Status :</b>Facturé", new Date(), EventType.STATUS));
                }
                if (status.equals(BillStatus.PENDING)) {
                    this.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>Status :</b>Facture en attente", new Date(), EventType.STATUS));
                }
                if (status.equals(BillStatus.CONFIRMED)) {
                    this.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>Status :</b>Facture annulée", new Date(), EventType.STATUS));
                }
                if (status.equals(BillStatus.CANCELLED)) {
                    this.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>Status :</b>Facture validée", new Date(), EventType.STATUS));
                }
                if (status.equals(BillStatus.PAID)) {
                    this.addComment(organization, new BillComment(Optional.empty(), uuid, login, "<b>Status :</b>Facture encaissée", new Date(), EventType.STATUS));
                }
            }
            return setted;
        }).thenCompose(setted ->
                get(organization, uuid).thenCompose(bill ->
                        buildIndexableBill(organization, bill.get())
                                .thenCompose(indexableOrder -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableOrder))
                                .thenApply(res -> setted)
                )
        );
    }

    @Override
    public CompletionStage<api.v1.models.Facture> serialize(String organization, Bill facture, Optional<Tuple2<Date, Date>> exportDate, Boolean notExported) {
        return supplyAsync(() -> this.billsRepository.factureLines(facture.uuid))
                .thenCompose(lignes -> supplyAsync(() -> this.billsRepository.paiements(facture.uuid))
                        .thenCompose(paiements -> supplyAsync(() -> this.billsRepository.avoirs(facture.uuid))
                                .thenCompose(avoirs -> this.missionClient.getInterventionsFromOrder(organization, facture.order, new String[]{"DONE"})
                                        .thenCompose(interventions -> CompletableFutureUtils.sequence(lignes.stream().map(ligne -> serializeLine(organization, ligne, interventions).toCompletableFuture()).collect(Collectors.toList()))
                                                .thenCompose(serializedLines -> {
                                                    List<api.v1.models.Paiement> payments = paiements.stream().filter(exportDate.isPresent() ? p -> p.exportdate.isPresent() && p.exportdate.get().after(exportDate.get()._1) && p.exportdate.get().before(exportDate.get()._2) : p -> true)
                                                            .filter(notExported ? p -> !p.exportdate.isPresent() : p -> true)
                                                            .map(api.v1.models.Paiement::serialize).collect(Collectors.toList());
                                                    return CompletableFutureUtils.sequence(avoirs.stream().filter(exportDate.isPresent() ? a -> a.exportdate.isPresent() && a.exportdate.get().after(exportDate.get()._1) && a.exportdate.get().before(exportDate.get()._2) : a -> true)
                                                            .filter(notExported ? a -> !a.exportdate.isPresent() : a -> true)
                                                            .map(a -> CompletableFutureUtils.sequence(lignes.stream().filter(l -> l.creditnote.isPresent() && l.creditnote.get().equals(a.uuid)).map(ligne -> serializeLine(organization, ligne, interventions).toCompletableFuture()).collect(Collectors.toList()))
                                                                    .thenApply(creditNotesLigne -> api.v1.models.Avoir.serialize(a, creditNotesLigne)).toCompletableFuture()).collect(Collectors.toList()))
                                                            .thenApply(creditNotes -> Facture.serialize(facture, serializedLines, payments, creditNotes));
                                                })))));
    }

    @Override
    public CompletionStage<FactureWithDetails> serializeWithDetails(String organization, Bill facture) {
        return serialize(organization, facture, Optional.empty(), false).thenCompose(fullBill -> this.ordersService.get(organization, fullBill.order).thenCompose(order -> this.ordersService.serializeOrder(organization, order.get()))
                .thenCompose(finalOrder -> this.missionClient.getInterventionsFromBill(organization, fullBill.uuid)
                        .thenApply(interventions -> {
                            Optional<String> address = interventions.size() == 1 ? ((DraftIntervention) interventions.get(0)).estateAddress : Optional.empty();
                            return FactureWithDetails.serialize(fullBill, finalOrder.get().establishment.get().account, finalOrder.get(), interventions, address);
                        })));
    }


    @Override
    public CompletionStage<Optional<Bill>> get(String organization, String uuid) {
        return supplyAsync(() -> this.billsRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<Bill>> getFromName(String organization, String name) {
        return supplyAsync(() -> this.billsRepository.getFromName(organization, name));
    }

    @Override
    public CompletionStage<Optional<CreditNote>> getCreditNoteFromName(String name) {
        return supplyAsync(() -> this.billsRepository.getCreditNoteFromName(name));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return supplyAsync(() -> {
            searchService.delete(organization, ELASTIC_OBJECT_TYPE, uuid);
            return this.billsRepository.delete(organization, uuid);
        });
    }

    @Override
    public CompletionStage<Optional<String>> addAvoir(String organization, String factureUuid, CreditNote creditNote) {
        return CompletableFuture.supplyAsync(() -> billsRepository.getLastCreditNoteName(organization))
                .thenCompose(lastCreditNoteName -> {
                    final String creditNoteName = getNewName("V", lastCreditNoteName);
                    CreditNote finalCreditNote = new CreditNote(Optional.of(creditNote.uuid), creditNoteName, creditNote.date, Optional.empty());
                    return CompletableFuture.supplyAsync(() -> this.billsRepository.addAvoir(factureUuid, finalCreditNote)).thenCompose(uuid -> {
                        if (uuid.isPresent()) {
                            return CompletableFuture.supplyAsync(() -> this.billsRepository.get(organization, factureUuid)).thenCompose(facture -> {
                                if (facture.isPresent()) {
                                    return CompletableFuture.supplyAsync(() -> this.billsRepository.factureLines(factureUuid))
                                            .thenCompose(lignes -> CompletableFutureUtils.sequence(lignes.stream().map(ligne -> CompletableFuture.supplyAsync(() -> this.billsRepository.setCreditNote(ligne, uuid.get()))).collect(Collectors.toList())))
                                            .thenApply(results -> {
                                                if (results.stream().anyMatch(r -> !r)) {
                                                    return Optional.empty();
                                                } else {
                                                    buildIndexableBill(organization, facture.get())
                                                            .thenCompose(indexableOrder -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableOrder));
                                                    return uuid;
                                                }
                                            });
                                } else {
                                    return CompletableFuture.completedFuture(Optional.empty());
                                }
                            });
                        } else {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                    });
                });
    }

    @Override
    public CompletionStage<Optional<Bill>> update(String organization, Bill bill, List<BillLine> lines) {
        if (bill.status.getId().equals(BillStatus.CONFIRMED.getId())) {
            return supplyAsync(() -> billsRepository.getLastBillName(organization))
                    .thenCompose(lastBillName -> {
                        final String billName = getNewName("B", lastBillName);
                        final Bill finalBill = new Bill(Optional.of(bill.uuid), billName, bill.accompte, bill.status, bill.recoverystatus, bill.order, bill.deadline, Optional.empty());
                        return updateBill(organization, finalBill, lines).thenApply(updatedBill -> updatedBill);
                    });
        } else {
            return updateBill(organization, bill, lines).thenApply(updatedBill -> updatedBill);
        }
    }

    @Override
    public CompletionStage<Boolean> setExported(String organization, Date date) {
        return CompletableFuture.supplyAsync(() -> this.billsRepository.getToExport(organization)).thenCompose(factures -> CompletableFutureUtils.sequence(factures.stream().map(facture ->
                CompletableFuture.supplyAsync(() -> this.billsRepository.factureLines(facture.uuid))
                        .thenCompose(lignes -> CompletableFuture.supplyAsync(() -> this.billsRepository.paiements(facture.uuid))
                                .thenCompose(paiements -> CompletableFuture.supplyAsync(() -> this.billsRepository.avoirs(facture.uuid))
                                        .thenCompose(avoirs -> {
                                            if (!facture.exportdate.isPresent() || paiements.stream().anyMatch(p -> !p.exportdate.isPresent()) || avoirs.stream().anyMatch(a -> !a.exportdate.isPresent())) {
                                                return CompletableFuture.supplyAsync(() -> this.billsRepository.setExportDate(facture, facture.exportdate.orElse(date), !facture.exportdate.isPresent() && avoirs.size() == 0))
                                                        .thenCompose(done1 -> {
                                                            if (done1) {
                                                                CompletionStage<List<Boolean>> paiementsDone = CompletableFutureUtils.sequence(paiements.stream().filter(p -> !p.exportdate.isPresent()).map(p -> supplyAsync(() -> this.billsRepository.setExportDate(p, date))).collect(Collectors.toList()));
                                                                CompletionStage<List<Boolean>> avoirsDone = CompletableFutureUtils.sequence(avoirs.stream().filter(a -> !a.exportdate.isPresent()).map(a -> supplyAsync(() -> this.billsRepository.setExportDate(a, date))).collect(Collectors.toList()));
                                                                return paiementsDone.thenCompose(done2 -> {
                                                                    if (done2.stream().allMatch(d -> d)) {
                                                                        return avoirsDone.thenApply(done3 -> done3.stream().allMatch(d -> d));
                                                                    } else {
                                                                        return CompletableFuture.completedFuture(false);
                                                                    }
                                                                });
                                                            } else {
                                                                return CompletableFuture.completedFuture(false);
                                                            }
                                                        })
                                                        .thenCompose(done ->
                                                                get(organization, facture.uuid).thenCompose(bill ->
                                                                        buildIndexableBill(organization, bill.get())
                                                                                .thenCompose(indexableOrder -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableOrder))
                                                                                .thenApply(res -> done)
                                                                )
                                                        );
                                            } else {
                                                return CompletableFuture.completedFuture(true);
                                            }
                                        }))
                        )).collect(Collectors.toList()))
                .thenApply(res -> res.stream().allMatch(r -> r)));

    }

    @Override
    public CompletionStage<List<BillComment>> getComments(String organization, String uuid) {
        return supplyAsync(() -> this.billsRepository.getComments(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<BillComment>> addComment(String organization, BillComment billComment) {
        return supplyAsync(() -> this.billsRepository.addComment(organization, billComment));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.BillComment>> serializeComment(String organization, BillComment
            comment) {
        if (comment.idUser.isPresent()) {
            return usersService.get(organization, comment.idUser.get())
                    .thenApply(user -> user.map(u -> api.v1.models.BillComment.serialize(comment, Optional.of(u))));
        } else {
            return CompletableFuture.completedFuture(Optional.of(api.v1.models.BillComment.serialize(comment, Optional.empty())));
        }
    }

    @Override
    public CompletionStage<File> generatePdfBill(String organization, String billId) {
        return get(organization, billId)
                .thenCompose(bill -> serializeWithDetails(organization, bill.get()))
                .thenCompose(bill ->
                        CompletableFutureUtils.sequence(
                                bill
                                        .interventions
                                        .stream()
                                        .map(i -> missionClient.listPrestationByInterventionId(i.getId()).toCompletableFuture())
                                        .collect(Collectors.toList())
                        )
                                .thenApply(interventions -> {
                                    Set<Prestation> ls = new HashSet<>();
                                    interventions.forEach(ls::addAll);
                                    return new Tuple2<FactureWithDetails, Set<Prestation>>(bill, ls);
                                })
                )
                .thenCompose(tuple ->
                        CompletableFutureUtils.sequence(
                                tuple
                                        ._2()
                                        .stream()
                                        .map(p -> this.estateClient.getEstate(organization, p.estate.get()).toCompletableFuture())
                                        .collect(Collectors.toList())
                        )
                                .thenApply(estates -> {
                                    Set<Estate> e = estates
                                            .stream()
                                            .filter(Optional::isPresent)
                                            .map(Optional::get)
                                            .collect(Collectors.toSet());
                                    return new Tuple3<FactureWithDetails, Set<Prestation>, Set<Estate>>(tuple._1(), tuple._2(), e);
                                })
                )
                .thenApply(tuple -> {
                    AbstractPdfDocument pdf = new BasicBillDocument(addressesService, organization, tuple._1(), tuple._2(), tuple._3());
                    try {
                        File document = pdf.generatePdfDocument("Facture " + tuple._1().bill.name + ".pdf");
                        InputStream inputStream = new ByteArrayInputStream(document.getContent().toByteArray());
                        this.fileService.upload(organization, billId, "bills", inputStream, document.getName(), document.getContent().size(), "application/pdf");
                        inputStream.close();
                        return document;
                    } catch (IOException e) {
                        log.error("Error with stream during pdf generation", e);
                    }

                    return null;
                });
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableBill>>> getOverviews(final String organization,
                                                                              final Pageable pageable) {
        return this.getElasticResult(ELASTIC_OBJECT_TYPE, organization, pageable);
    }

    public CompletionStage<Boolean> setMapping(String organization) {
        Map<String, Map<String, String>> mapping = new HashMap<>();
        for (String fieldName : new String[]{"exportDate"}) {
            Map<String, String> fieldMapping = new HashMap<>();
            fieldMapping.put("type", "date");
            fieldMapping.put("format", "epoch_millis");
            mapping.put(fieldName, fieldMapping);
        }
        return searchService.setMapping(organization, ELASTIC_OBJECT_TYPE, mapping);
    }

    public CompletionStage<Boolean> reindex(final String organization) {
        return setMapping(organization)
                .thenCompose(res -> getAll(organization).thenCompose(bills -> doIndexOrder(organization, bills)));
    }

    protected CompletionStage<Boolean> doIndexOrder(final String organization, final List<Bill> bills) {
        if (!bills.isEmpty()) {
            return serializeWithDetails(organization, bills.get(0))
                    .thenApply(bill -> buildIndexableBill(organization, bill))
                    .thenCompose(indexableBill -> searchService.upsert(organization, ELASTIC_OBJECT_TYPE, indexableBill))
                    .thenCompose(result -> {
                        bills.remove(0);
                        return doIndexOrder(organization, bills).thenApply(r -> r && result);
                    });
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    public CompletionStage<IndexableBill> buildIndexableBill(final String organization, final Bill bill) {
        return serializeWithDetails(organization, bill)
                .thenApply(billWithDetail -> buildIndexableBill(organization, billWithDetail));
    }

    public IndexableBill buildIndexableBill(final String organization, final FactureWithDetails bill) {
        OrderForBillIndexable orderForBillIndexable = new OrderForBillIndexable();
        orderForBillIndexable
                .setId(bill.order.uuid)
                .setName(bill.order.name);
        bill.order.referenceNumber.ifPresent(orderForBillIndexable::setReferenceNumber);

        AccountForIndexableBill accountForIndexableBill = new AccountForIndexableBill();
        accountForIndexableBill.setId(bill.account.uuid);
        bill.account.entity.ifPresent(entity -> accountForIndexableBill.setName(entity.name));

        IndexableBill indexableBill = new IndexableBill(
                bill.bill.uuid,
                orderForBillIndexable,
                bill.bill.name,
                null,
                accountForIndexableBill,
                bill.order.market.map(fullMarket -> fullMarket.name + " - " + fullMarket.marketNumber).orElse(null),
                bill.address.orElse(null),
                bill.bill.status,
                bill.bill.exportDate
        );

        if (!bill.bill.creditnotes.isEmpty()) {
            indexableBill.setCreditNote(bill.bill.creditnotes.get(0).name);
        }

        return indexableBill;
    }
}
