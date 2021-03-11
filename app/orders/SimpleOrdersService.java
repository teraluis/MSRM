package orders;

import accounts.AccountsService;
import addresses.AddressesService;
import api.v1.models.*;
import core.models.People;
import agencies.AgenciesService;
import core.search.Pageable;
import core.search.AbstractSearchService;
import core.CompletableFutureUtils;
import core.EventType;
import core.models.Address;
import core.models.AddressWithRole;
import core.search.SearchService;
import entities.EntitiesService;
import entities.Entity;
import establishments.EstablishmentAddressRole;
import establishments.EstablishmentDelegateRole;
import establishments.EstablishmentPeopleRole;
import establishments.EstablishmentsService;
import estimates.EstimatesService;
import markets.BpuReference;
import markets.MarketPeopleRole;
import markets.MarketsService;
import missionclient.MissionClient;
import core.models.Prestation;
import missionclient.PrestationWithEstate;
import core.models.TechnicalAct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import people.PeopleAddressRole;
import people.PeopleService;
import scala.Tuple2;
import users.UsersService;
import core.search.PaginatedResult;

import javax.inject.Inject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SimpleOrdersService extends AbstractSearchService<IndexableOrder> implements OrdersService {

    protected final Logger logger = LoggerFactory.getLogger(SimpleOrdersService.class);
    protected final OrdersRepository ordersRepository;
    protected final MissionClient missionClient;
    protected final MarketsService marketsService;
    protected final EstablishmentsService establishmentsService;
    protected final AccountsService accountsService;
    protected final SearchService searchService;
    protected final PeopleService peopleService;
    protected final EstimatesService estimatesService;
    protected final AddressesService addressesService;
    protected final EntitiesService entitiesService;
    protected final UsersService usersService;
    protected final AgenciesService agenciesService;

    @Inject
    public SimpleOrdersService(final OrdersRepository ordersRepository, final MissionClient missionClient, final MarketsService marketsService, final EstablishmentsService establishmentsService, final EntitiesService entitiesService,
                               final AccountsService accountsService, final SearchService searchService, final PeopleService peopleService, final EstimatesService estimatesService, AddressesService addressesService, UsersService usersService,
                               final AgenciesService agenciesService) {
        super(IndexableOrder.class, searchService);
        this.ordersRepository = ordersRepository;
        this.missionClient = missionClient;
        this.marketsService = marketsService;
        this.establishmentsService = establishmentsService;
        this.accountsService = accountsService;
        this.searchService = searchService;
        this.peopleService = peopleService;
        this.estimatesService = estimatesService;
        this.addressesService = addressesService;
        this.entitiesService = entitiesService;
        this.usersService = usersService;
        this.agenciesService = agenciesService;
    }

    @Override
    public CompletionStage<List<ReportDestination>> getReportDestinationsFromOrder(String organization, String orderUuid, Optional<String> estimateId, Optional<String> marketId, Optional<String> establishmentId) {
        return CompletableFuture.supplyAsync(() -> ordersRepository.getReportDestinationsFromOrder(organization, orderUuid))
                .thenCompose(reportDestinations -> establishmentId.map(id -> getReportDestinationsFromEstablishment(organization, id)
                        .thenApply(repDest -> {
                            reportDestinations.addAll(repDest);
                            return reportDestinations;
                        }))
                        .orElseGet(() -> CompletableFuture.completedFuture(reportDestinations)))
                .thenCompose(reportDestinations -> marketId.map(id -> getReportDestinationsFromMarket(organization, id)
                        .thenApply(repDest -> {
                            reportDestinations.addAll(repDest);
                            return reportDestinations;
                        }))
                        .orElseGet(() -> CompletableFuture.completedFuture(reportDestinations)));
    }

    public CompletionStage<List<ReportDestination>> getReportDestinationsFromEstablishment(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> ordersRepository.getReportDestinationsFromEstablishment(organization, establishmentId))
                .thenCompose(reportDestinations -> establishmentsService.getDelegatesByRole(organization, establishmentId, EstablishmentDelegateRole.REPORT.toString())
                        .thenCompose(establishmentWithRoles -> establishmentWithRoles.size() == 0 ? CompletableFuture.completedFuture(Optional.<List<String>>empty()) : CompletableFuture.completedFuture(Optional.<List<String>>of(establishmentWithRoles.stream().map(e -> e.establishment).collect(Collectors.toList()))))
                        .thenCompose(e -> e.map(establishments -> CompletableFuture.completedFuture(establishments.stream().map(id -> new ReportDestination(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(id))).collect(Collectors.toList()))
                                .thenApply(repDest -> {
                                    reportDestinations.addAll(repDest);
                                    return reportDestinations;
                                }))
                                .orElseGet(() -> CompletableFuture.completedFuture(reportDestinations))))
                .thenCompose(reportDestinations -> establishmentsService.getPeopleByRole(organization, establishmentId, EstablishmentPeopleRole.REPORT.toString())
                        .thenCompose(peopleWithRoles -> peopleWithRoles.size() == 0 ? CompletableFuture.completedFuture(Optional.<List<String>>empty()) : CompletableFuture.completedFuture(Optional.<List<String>>of(peopleWithRoles.stream().map(p -> p.people).collect(Collectors.toList()))))
                        .thenCompose(p -> p.map(people -> CompletableFuture.completedFuture(people.stream().map(id -> new ReportDestination(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(id), Optional.empty())).collect(Collectors.toList()))
                                .thenApply(repDest -> {
                                    reportDestinations.addAll(repDest);
                                    return reportDestinations;
                                }))
                                .orElseGet(() -> CompletableFuture.completedFuture(reportDestinations))));
    }

    public CompletionStage<List<ReportDestination>> getReportDestinationsFromMarket(String organization, String marketId) {
        return CompletableFuture.supplyAsync(() -> ordersRepository.getReportDestinationsFromMarket(organization, marketId))
                .thenCompose(reportDestinations -> marketsService.getPeopleByRole(organization, marketId, MarketPeopleRole.REPORT.toString())
                        .thenCompose(peopleWithRoles -> peopleWithRoles.size() == 0 ? CompletableFuture.completedFuture(Optional.<List<String>>empty()) : CompletableFuture.completedFuture(Optional.<List<String>>of(peopleWithRoles.stream().map(p -> p.people).collect(Collectors.toList()))))
                        .thenCompose(p -> p.map(people -> CompletableFuture.completedFuture(people.stream().map(id -> new ReportDestination(Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.empty(), Optional.of(id), Optional.empty())).collect(Collectors.toList()))
                                .thenApply(repDest -> {
                                    reportDestinations.addAll(repDest);
                                    return reportDestinations;
                                }))
                                .orElseGet(() -> CompletableFuture.completedFuture(reportDestinations))));
    }

    public CompletionStage<api.v1.models.ReportDestination> serializeReportDestination(String organization, ReportDestination report) {
        return report.establishment.map(id -> establishmentsService.get(organization, id)
                .thenCompose(establishment -> establishment.map(e -> establishmentsService.serialize(organization, e)
                        .thenApply(finalEstablishment -> api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.empty(), Optional.of(finalEstablishment))))
                        .orElseGet(() -> CompletableFuture.completedFuture(api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.empty(), Optional.empty())))))
                .orElseGet(() -> report.people.map(id -> peopleService.get(organization, id)
                        .thenCompose(people -> people.map(p -> peopleService.serialize(organization, p)
                                .thenApply(finalPeople -> api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.of(finalPeople), Optional.empty())))
                                .orElseGet(() -> CompletableFuture.completedFuture(api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.empty(), Optional.empty())))))
                        .orElseGet(() -> report.address.map(id -> addressesService.get(organization, report.address.get())
                                .thenApply(address -> address.map(finalAddress -> api.v1.models.ReportDestination.serialize(report, Optional.of(finalAddress.serialize()), Optional.empty(), Optional.empty()))
                                        .orElseGet(() -> api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.empty(), Optional.empty()))))
                                .orElse(CompletableFuture.completedFuture(api.v1.models.ReportDestination.serialize(report, Optional.empty(), Optional.empty(), Optional.empty())))));
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Order order, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.add(organization, order))
                .thenApply(uuid -> {
                    if (login.isPresent() && uuid.isPresent()) {
                        this.addComment(organization, new OrderComment(Optional.empty(), uuid.get(), login, "Commande reçue", new Date(), EventType.STATUS));
                    }
                    buildIndexableOrder(organization, order).thenCompose(indexableOrder -> searchService.upsert(organization, "orders", indexableOrder));

                    return uuid;
                });
    }

    @Override
    public CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> getPrestationsWithEstate(String organization) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getAll(organization)).thenCompose(orders -> {
            List<CompletableFuture<Tuple2<Order, List<PrestationWithEstate>>>> results = orders.stream().map(o -> this.missionClient.getPrestationWithEstateFromOrder(organization, o.uuid).thenApply(prestations -> new Tuple2<>(o, prestations)).toCompletableFuture()).collect(Collectors.toList());
            return CompletableFutureUtils.sequence(results);
        });
    }

    @Override
    public CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getPage(organization, offset, length)).thenCompose(orders -> {
            List<CompletableFuture<Tuple2<Order, List<PrestationWithEstate>>>> results = orders.stream().map(o -> this.missionClient.getPrestationWithEstateFromOrder(organization, o.uuid).thenApply(prestations -> new Tuple2<>(o, prestations)).toCompletableFuture()).collect(Collectors.toList());
            return CompletableFutureUtils.sequence(results);
        });
    }

    @Override
    public CompletionStage<List<Order>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Order>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.searchPage(organization, pattern, offset, length)).thenCompose(orders -> {
            List<CompletableFuture<Tuple2<Order, List<PrestationWithEstate>>>> results = orders.stream().map(o -> this.missionClient.getPrestationWithEstateFromOrder(organization, o.uuid).thenApply(prestations -> new Tuple2<>(o, prestations)).toCompletableFuture()).collect(Collectors.toList());
            return CompletableFutureUtils.sequence(results);
        });
    }

    @Override
    public CompletionStage<Optional<Order>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<Order>> getFromMarket(String organization, String marketUuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getFromMarket(organization, marketUuid));
    }

    @Override
    public CompletionStage<List<Order>> getFromEstimate(String organization, String estimateUuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getFromEstimate(organization, estimateUuid));
    }

    @Override
    public CompletionStage<List<Order>> getFromEstablishment(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getFromEstablishment(organization, establishmentId));
    }

    @Override
    public CompletionStage<List<Order>> getFromAccount(String organization, String accountId) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getFromAccount(organization, accountId));
    }

    @Override
    public CompletionStage<List<Order>> getFromEstate(String organization, String estateId) {
        return missionClient.getOrdersFromEstate(organization, estateId)
                .thenApply(orders -> this.ordersRepository.getFromList(organization, orders));
    }

    @Override
    public CompletionStage<Optional<Order>> getFromBill(String organization, String billUuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getFromBill(organization, billUuid));
    }

    @Override
    public CompletionStage<List<OrderLine>> getOrderLines(String organization, String orderUuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getOrderLines(organization, orderUuid));
    }

    @Override
    public CompletionStage<Optional<Order>> checkName(String organization, String name) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.checkName(organization, name));
    }

    @Override
    public CompletionStage<Boolean> setStatus(String organization, String uuid, OrderStatus status, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.get(organization, uuid))
                .thenApply(orderStatusOld -> {
                    final boolean setStatus = this.ordersRepository.setStatus(organization, uuid, status);
                    if (login.isPresent() && orderStatusOld.isPresent() && setStatus) {
                        this.addComment(organization, new OrderComment(Optional.empty(), orderStatusOld.get().uuid, login, this.translateStatus(orderStatusOld.get().status.name()) + " -> " + this.translateStatus(status.name()), new Date(), EventType.STATUS))
                                .thenApply(comment -> {
                                    if (comment.isPresent()) {
                                        logger.info("comment status has been added");
                                        return true;
                                    } else {
                                        logger.error("problem when adding a comment");
                                        return false;
                                    }
                                });
                    }
                    return setStatus;
                })
                // Update search plateforme
                .thenCompose(done -> buildIndexableOrder(organization, uuid).thenCompose(indexableOrder -> {
                    if (indexableOrder.isPresent()) {
                        return searchService.upsert(organization, "orders", indexableOrder.get()).thenApply(unused -> done);
                    }

                    return CompletableFuture.supplyAsync(() -> done);
                }));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.Order>> serializeOrder(String organization, Order order) {
        return getOrderLines(organization, order.uuid)
                .thenCompose(lines -> getReportDestinationsFromOrder(organization, order.uuid, order.estimate, order.market, order.establishment)
                        .thenCompose(reportDestinations -> CompletableFutureUtils.sequence(reportDestinations.stream().map(report -> serializeReportDestination(organization, report).toCompletableFuture()).collect(Collectors.toList()))
                                .thenCompose(finalReportDestinations -> accountsService.get(organization, order.account)
                                        .thenCompose(account -> account.map(a -> accountsService.serialize(organization, a)
                                                .thenApply(Optional::of))
                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Account>empty())))
                                        .thenCompose(finalAccount -> order.purchaserContact.map(id -> peopleService.get(organization, id)
                                                .thenCompose(contact -> contact.map(p -> peopleService.serialize(organization, p)
                                                        .thenApply(Optional::of))
                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))))
                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))
                                                .thenCompose(finalContact -> order.commercial.map(id -> usersService.get(organization, id)
                                                        .thenCompose(commercial -> commercial.map(c -> CompletableFuture.completedFuture(Optional.of(User.serialize(c))))
                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<User>empty()))))
                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<User>empty()))
                                                        .thenCompose(finalCommercial -> order.agency.map(id -> agenciesService.get(organization, id)
                                                                .thenCompose(agency -> agency.map(a -> agenciesService.serialize(organization, a)
                                                                        .thenApply(Optional::of))
                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Agency>empty()))))
                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Agency>empty()))
                                                                .thenCompose(finalAgency -> order.establishment.map(id -> establishmentsService.get(organization, id)
                                                                        .thenCompose(establishment -> establishment.map(e -> establishmentsService.serializeFull(organization, e, true)
                                                                                .thenApply(Optional::of))
                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<FullEstablishment>empty()))))
                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<FullEstablishment>empty()))
                                                                        .thenCompose(finalEstablishment -> order.billedEstablishment.map(id -> establishmentsService.get(organization, id)
                                                                                .thenCompose(billed -> billed.map(b -> establishmentsService.serialize(organization, b)
                                                                                        .thenApply(Optional::of))
                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Establishment>empty()))))
                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Establishment>empty()))
                                                                                .thenCompose(finalBilledEstablishment -> order.billedContact.map(id -> peopleService.get(organization, id)
                                                                                        .thenCompose(billed -> billed.map(b -> peopleService.serialize(organization, b)
                                                                                                .thenApply(Optional::of))
                                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))))
                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))
                                                                                        .thenCompose(finalBilledContact -> order.payerEstablishment.map(id -> establishmentsService.get(organization, id)
                                                                                                .thenCompose(billed -> billed.map(b -> establishmentsService.serialize(organization, b)
                                                                                                        .thenApply(Optional::of))
                                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Establishment>empty()))))
                                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<Establishment>empty()))
                                                                                                .thenCompose(finalPayerEstablishment -> order.payerContact.map(id -> peopleService.get(organization, id)
                                                                                                        .thenCompose(billed -> billed.map(b -> peopleService.serialize(organization, b)
                                                                                                                .thenApply(Optional::of))
                                                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))))
                                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.<People>empty()))
                                                                                                        .thenCompose(finalPayerContact -> finalAccount.map(account -> order.market.map(id -> marketsService.get(organization, id)
                                                                                                                .thenCompose(market -> market.map(m -> marketsService.serialize(organization, m)
                                                                                                                        // Serialize with market
                                                                                                                        .thenApply(finalMarket -> Optional.of(api.v1.models.Order.serialize(order, account, Optional.of(finalMarket), Optional.empty(), finalEstablishment, finalContact, finalCommercial, lines.stream().map(api.v1.models.OrderLine::serialize).collect(Collectors.toList()), finalReportDestinations, finalAgency, finalBilledEstablishment, finalBilledContact, finalPayerEstablishment, finalPayerContact))))
                                                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.of(api.v1.models.Order.serialize(order, account, Optional.empty(), Optional.empty(), finalEstablishment, finalContact, finalCommercial, lines.stream().map(api.v1.models.OrderLine::serialize).collect(Collectors.toList()), finalReportDestinations, finalAgency, finalBilledEstablishment, finalBilledContact, finalPayerEstablishment, finalPayerContact))))))
                                                                                                                .orElseGet(() -> order.estimate.map(id -> estimatesService.get(organization, order.estimate.get())
                                                                                                                        .thenCompose(estimate -> estimate.map(e -> CompletableFuture.completedFuture(Estimate.serialize(e, Optional.empty(), Optional.of(account)))
                                                                                                                                // Serialize with estimate
                                                                                                                                .thenApply(finalEstimate -> Optional.of(api.v1.models.Order.serialize(order, account, Optional.empty(), Optional.of(finalEstimate), finalEstablishment, finalContact, finalCommercial, lines.stream().map(api.v1.models.OrderLine::serialize).collect(Collectors.toList()), finalReportDestinations, finalAgency, finalBilledEstablishment, finalBilledContact, finalPayerEstablishment, finalPayerContact))))
                                                                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.of(api.v1.models.Order.serialize(order, account, Optional.empty(), Optional.empty(), finalEstablishment, finalContact, finalCommercial, lines.stream().map(api.v1.models.OrderLine::serialize).collect(Collectors.toList()), finalReportDestinations, finalAgency, finalBilledEstablishment, finalBilledContact, finalPayerEstablishment, finalPayerContact))))))
                                                                                                                        // Serialize for individual or establishment without estimate
                                                                                                                        .orElseGet(() -> CompletableFuture.completedFuture(Optional.of(api.v1.models.Order.serialize(order, account, Optional.empty(), Optional.empty(), finalEstablishment, finalContact, finalCommercial, lines.stream().map(api.v1.models.OrderLine::serialize).collect(Collectors.toList()), finalReportDestinations, finalAgency, finalBilledEstablishment, finalBilledContact, finalPayerEstablishment, finalPayerContact))))))
                                                                                                                // Empty if no account
                                                                                                                .orElseGet(() -> CompletableFuture.completedFuture(Optional.<api.v1.models.Order>empty()))))))))))))));
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableOrder>>> getOverviews(final String organization,
                                                                               final Pageable pageable) {
        return this.getElasticResult("orders", organization, pageable);
    }

    @Override
    public CompletionStage<List<String>> getEstablishmentsWithOrder(String organization) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getEstablishmentsWithOrder(organization));
    }

    @Override
    public CompletionStage<Optional<Order>> update(String organization, Order order, Optional<String> login) {
        buildIndexableOrder(organization, order).thenCompose(indexableOrder -> searchService.upsert(organization, "orders", indexableOrder));
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.get(organization, order.uuid))
                .thenCompose(
                        oldOrder -> CompletableFuture.supplyAsync(() -> this.ordersRepository.update(organization, order))
                                .thenApply(
                                        orderUpdated -> {
                                            if (login.isPresent() && orderUpdated.isPresent()) {
                                                final boolean deadline = oldOrder.get().deadline.map(dl -> orderUpdated.get().deadline.isPresent() && orderUpdated.get().deadline.get().equals(dl)).orElse(!orderUpdated.get().deadline.isPresent());

                                                SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yy");
                                                if (!oldOrder.get().status.name().equals(orderUpdated.get().status.name())) {
                                                    this.addComment(organization, new OrderComment(Optional.empty(), orderUpdated.get().uuid, login, "<b>" + this.translateStatus(oldOrder.get().status.name()) + "</b> -> " + this.translateStatus(orderUpdated.get().status.name()), new Date(), EventType.MODIFICATION));
                                                }
                                                if (!deadline && oldOrder.get().deadline.isPresent()) {
                                                    this.addComment(organization, new OrderComment(Optional.empty(), orderUpdated.get().uuid, login, "La date limite de livraison a été modifiée : " + formatDate.format(oldOrder.get().deadline.get()) + " -> " + formatDate.format(orderUpdated.get().deadline.get()), new Date(), EventType.MODIFICATION));
                                                }
                                                if (oldOrder.get().referenceNumber.isPresent() && !oldOrder.get().referenceNumber.get().equals(orderUpdated.get().referenceNumber.get())) {
                                                    this.addComment(organization, new OrderComment(Optional.empty(), orderUpdated.get().uuid, login, "Numéro bon commande modifié : " + oldOrder.get().referenceNumber.get() + " -> " + orderUpdated.get().referenceNumber.get(), new Date(), EventType.MODIFICATION));
                                                }
                                            }
                                            if (oldOrder.isPresent() && (!oldOrder.get().referenceNumber.isPresent() || !oldOrder.get().deadline.isPresent() || !oldOrder.get().adviceVisit.isPresent())) {
                                                this.addComment(organization, new OrderComment(Optional.empty(), orderUpdated.get().uuid, login, "Descriptif de la commande saisie", new Date(), EventType.MODIFICATION));
                                            }
                                            doIndexOrder(organization, new ArrayList(Collections.singletonList(orderUpdated.get())));
                                            return orderUpdated;
                                        }
                                )
                );
    }

    public String translateStatus(String status) {
        switch (status.toLowerCase()) {
            case "received":
                return "Commande reçue";
            case "filled":
                return "Commande renseignée";
            case "production":
                return "Commande en production";
            case "billable":
                return "Commande facturable";
            case "honored":
                return "Commande honorée";
            case "closed":
                return "Commande fermée";
            case "canceled":
                return "Commande annulée";
            default:
                return "Commande inconnue";
        }
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture
                .supplyAsync(() -> {
                    searchService.delete(organization, "orders", uuid);
                    return this.ordersRepository.delete(organization, uuid);
                });
    }

    @Override
    public CompletionStage<Boolean> setOrderLines(String organization, String orderUuid, List<OrderLine> orderLines, List<String> orderLinesToDelete) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.setOrderLines(organization, orderUuid, orderLines, orderLinesToDelete))
                .thenCompose(result -> {
                    if (result) {
                        return get(organization, orderUuid)
                                .thenCompose(order -> {
                                    if (order.isPresent() && order.get().market.isPresent()) {
                                        return CompletableFutureUtils.sequence(orderLines.stream().map(line -> (line.refbpu.isPresent() || line.designation.isPresent()) ? marketsService.addReferenceIfNotExist(order.get().market.get(), new BpuReference(line.refbpu, line.designation, line.price)).toCompletableFuture() : CompletableFuture.completedFuture(Optional.<String>empty())).collect(Collectors.toList()))
                                                .thenApply(done -> true);
                                    } else {
                                        return CompletableFuture.completedFuture(true);
                                    }
                                });
                    } else {
                        return CompletableFuture.completedFuture(false);
                    }
                });
    }

    public CompletionStage<Optional<IndexableOrder>> buildIndexableOrder(final String organization, final String orderId) {
        return get(organization, orderId).thenCompose(maybeOrder -> {
            if (maybeOrder.isPresent()) {
                return buildIndexableOrder(organization, maybeOrder.get()).thenApply(Optional::of);
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableOrder> buildIndexableOrder(final String organization, final Order order) {
        if (order.purchaserContact.isPresent()) {
            return this.peopleService.get(organization, order.purchaserContact.get())
                    .thenCompose(purchaserContact -> {
                        if (purchaserContact.isPresent()) {
                            return this.peopleService.serialize(organization, purchaserContact.get());
                        } else {
                            throw new RuntimeException("Failed to get purchaser_contact " + order.purchaserContact.get() + " from order " + order.uuid);
                        }
                    })
                    .thenCompose(purchaserContact -> {
                        if (order.establishment.isPresent()) {
                            return this.establishmentsService.getAddresses(organization, order.establishment.get())
                                    .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address)
                                            .thenApply(address -> new core.models.AddressWithRole(address.get().serialize(), addressWithRole.role))
                                            .toCompletableFuture())
                                            .collect(Collectors.toList()))
                                    )
                                    .thenCompose(addresses -> buildIndexableOrderWithMarket(organization, order, purchaserContact.uuid, purchaserContact.lastname.toUpperCase() + " " + purchaserContact.firstname, addresses));
                        } else {
                            // Individual
                            return buildIndexableOrderWithMarket(organization, order, purchaserContact.uuid, purchaserContact.lastname.toUpperCase() + " " + purchaserContact.firstname, purchaserContact.addresses);
                        }
                    });
        } else if (order.establishment.isPresent()) {
            return this.establishmentsService.get(organization, order.establishment.get())
                    .thenCompose(establishment -> {
                        if (establishment.isPresent()) {
                            return this.establishmentsService.serialize(organization, establishment.get());
                        } else {
                            throw new RuntimeException("Failed to get purchaser_establishment " + order.establishment.get() + " from order " + order.uuid);
                        }
                    })
                    .thenCompose(establishment -> this.establishmentsService.getAddresses(organization, order.establishment.get())
                            .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address)
                                    .thenApply(address -> new AddressWithRole(address.get().serialize(), addressWithRole.role))
                                    .toCompletableFuture())
                                    .collect(Collectors.toList()))
                            )
                            .thenCompose(addresses -> buildIndexableOrderWithMarket(organization, order, establishment.uuid, establishment.name, addresses)));
        } else {
            throw new RuntimeException("No purchaser in order " + order.uuid);
        }
    }

    protected CompletionStage<IndexableOrder> buildIndexableOrderWithMarket(final String organization, final Order order, final String purchaserId, final String purchaserName, List<AddressWithRole> addresses) {

        final Optional<Address> billingAddress;
        if (!addresses.isEmpty()) {
            Optional<AddressWithRole> address = addresses
                    .stream()
                    .filter(a -> a.role.equalsIgnoreCase(PeopleAddressRole.BILLING.toString()))
                    .findFirst();

            billingAddress = Optional.ofNullable(
                    address
                            .map(a -> a.address)
                            .orElseGet(() ->
                                    addresses
                                            .stream()
                                            .filter(a -> a.role.equalsIgnoreCase(PeopleAddressRole.MAIN.toString()))
                                            .findFirst()
                                            .map(a -> a.address)
                                            .orElseGet(null)
                            )
            );
        } else {
            billingAddress = Optional.empty();
        }

        return this.missionClient.getPrestationWithEstateFromOrder(organization, order.uuid).thenCompose(pestationWithEstate -> {
            Set<String> references = pestationWithEstate.stream().map(p -> p.estate.get().estateReference.orElseGet(() -> p.estate.get().adxReference)).collect(Collectors.toSet());
            return this.missionClient.listPrestationByOrderId(organization, order.uuid).thenCompose(orderPrestations -> {
                Map<String, Integer> prestations = new HashMap<>();
                for (Prestation p : orderPrestations) {
                    if (!p.technicalAct.isPresent()) {
                        continue;
                    }
                    TechnicalAct ta = p.technicalAct.get();
                    if (prestations.containsKey(ta.shortcut)) {
                        prestations.put(ta.shortcut, prestations.get(ta.shortcut) + 1);
                    } else {
                        prestations.put(ta.shortcut, 1);
                    }
                }
                if (order.establishment.isPresent()) {
                    // If professional account
                    return this.establishmentsService.get(organization, order.establishment.get()).thenCompose(establishment -> {
                        if (establishment.isPresent()) {
                            return this.accountsService.getFromEntity(organization, establishment.get().entity).thenCompose(account -> { // retrieve accountId
                                return this.establishmentsService.buildIndexableEstablishment(organization, establishment.get()).thenCompose(indexableEstablishment -> {
                                    return this.establishmentsService.getAddressesByRole(organization, establishment.get().uuid, EstablishmentAddressRole.BILLING.toString())
                                            .thenCompose(addressWithRoles -> {
                                                if (!addressWithRoles.isEmpty()) {
                                                    return this.addressesService.get(organization, addressWithRoles.get(0).address).thenApply(address -> {
                                                        Optional<String> addressName = Optional.empty();
                                                        if (address.isPresent()) {
                                                            if (address.get().address1.isPresent() && address.get().postCode.isPresent() && address.get().city.isPresent())
                                                                addressName = Optional.of(address.get().address1.get() + ", " + (address.get().address2.map(s -> s + ", ").orElse("")) + address.get().postCode.get() + " " + address.get().city.get());
                                                            else
                                                                addressName = address.get().gpsCoordinates.isPresent() ? address.get().gpsCoordinates : address.get().inseeCoordinates;
                                                        }
                                                        return addressName;
                                                    });
                                                }
                                                return CompletableFuture.completedFuture(Optional.empty());
                                            })
                                            .thenCompose(address -> {
                                                address.ifPresent(a -> indexableEstablishment.setAddress((String) a));
                                                if (account.isPresent() && account.get().entity.isPresent()) {
                                                    return this.entitiesService.get(organization, account.get().entity.get())
                                                            .thenCompose(entity -> this.usersService.get(organization, account.get().commercial)
                                                                    .thenApply(user -> new Tuple2<Optional<Entity>, Optional<users.User>>(entity, user))
                                                            )
                                                            .thenCompose(tuple -> { // retrieve accountName
                                                                if (tuple._1().isPresent()) {
                                                                    if (order.market.isPresent()) {
                                                                        return marketsService.get(organization, order.market.get()).thenApply(market ->
                                                                                new IndexableOrder(
                                                                                        order.uuid,
                                                                                        order.name,
                                                                                        order.status.name(),
                                                                                        order.created,
                                                                                        new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                                                                        Optional.of(new IndexableOrder.IdWithName(account.get().uuid, tuple._1().get().name)),
                                                                                        market.map(simpleMarket -> new IndexableOrder.IdWithName(simpleMarket.uuid, simpleMarket.name)),
                                                                                        order.description,
                                                                                        order.referenceNumber,
                                                                                        Optional.of(indexableEstablishment),
                                                                                        tuple._2(),
                                                                                        billingAddress,
                                                                                        order.received,
                                                                                        order.adviceVisit,
                                                                                        order.deadline,
                                                                                        order.assessment,
                                                                                        references,
                                                                                        prestations
                                                                                )
                                                                        );
                                                                    } else {
                                                                        return CompletableFuture.completedFuture(
                                                                                new IndexableOrder(
                                                                                        order.uuid,
                                                                                        order.name,
                                                                                        order.status.name(),
                                                                                        order.created,
                                                                                        new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                                                                        Optional.of(new IndexableOrder.IdWithName(account.get().uuid, tuple._1().get().name)),
                                                                                        Optional.empty(),
                                                                                        order.description,
                                                                                        order.referenceNumber,
                                                                                        Optional.of(indexableEstablishment),
                                                                                        tuple._2(),
                                                                                        billingAddress,
                                                                                        order.received,
                                                                                        order.adviceVisit,
                                                                                        order.deadline,
                                                                                        order.assessment,
                                                                                        references,
                                                                                        prestations
                                                                                )
                                                                        );
                                                                    }
                                                                } else {
                                                                    return CompletableFuture.completedFuture(
                                                                            new IndexableOrder(
                                                                                    order.uuid,
                                                                                    order.name,
                                                                                    order.status.name(),
                                                                                    order.created,
                                                                                    new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                                                                    Optional.empty(),
                                                                                    Optional.empty(),
                                                                                    order.description,
                                                                                    order.referenceNumber,
                                                                                    Optional.of(indexableEstablishment),
                                                                                    tuple._2(),
                                                                                    billingAddress,
                                                                                    order.received,
                                                                                    order.adviceVisit,
                                                                                    order.deadline,
                                                                                    order.assessment,
                                                                                    references,
                                                                                    prestations
                                                                            )
                                                                    );
                                                                }
                                                            });
                                                } else {
                                                    return CompletableFuture.completedFuture(
                                                            new IndexableOrder(
                                                                    order.uuid,
                                                                    order.name,
                                                                    order.status.name(),
                                                                    order.created,
                                                                    new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                                                    Optional.empty(),
                                                                    Optional.empty(),
                                                                    order.description,
                                                                    order.referenceNumber,
                                                                    Optional.of(indexableEstablishment),
                                                                    Optional.empty(),
                                                                    billingAddress,
                                                                    order.received,
                                                                    order.adviceVisit,
                                                                    order.deadline,
                                                                    order.assessment,
                                                                    references,
                                                                    prestations
                                                            )
                                                    );
                                                }
                                            });
                                });
                            });
                        } else {
                            return CompletableFuture.completedFuture(
                                    new IndexableOrder(
                                            order.uuid,
                                            order.name,
                                            order.status.name(),
                                            order.created,
                                            new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                            Optional.empty(),
                                            Optional.empty(),
                                            order.description,
                                            order.referenceNumber,
                                            Optional.empty(),
                                            Optional.empty(),
                                            billingAddress,
                                            order.received,
                                            order.adviceVisit,
                                            order.deadline,
                                            order.assessment,
                                            references,
                                            prestations
                                    )
                            );
                        }
                    });
                } else {
                    // If private account, purchaser and account are the same
                    return CompletableFuture.completedFuture(
                            new IndexableOrder(
                                    order.uuid,
                                    order.name,
                                    order.status.name(),
                                    order.created,
                                    new IndexableOrder.IdWithName(purchaserId, purchaserName),
                                    Optional.of(new IndexableOrder.IdWithName(purchaserId, purchaserName)),
                                    Optional.empty(),
                                    order.description,
                                    order.referenceNumber,
                                    Optional.empty(),
                                    Optional.empty(),
                                    billingAddress,
                                    order.received,
                                    order.adviceVisit,
                                    order.deadline,
                                    order.assessment,
                                    references,
                                    prestations
                            )
                    );
                }
            });
        });
    }

    public CompletionStage<Boolean> setMapping(String organization) {
        Map<String, Map<String, String>> mapping = new HashMap<>();
        for (String fieldName : new String[]{"created", "receive", "delivery", "assessment"}) {
            Map<String, String> fieldMapping = new HashMap<>();
            fieldMapping.put("type", "date");
            fieldMapping.put("format", "epoch_millis");
            mapping.put(fieldName, fieldMapping);
        }
        return searchService.setMapping(organization, "orders", mapping);
    }

    public CompletionStage<Boolean> reindex(final String organization) {
        return setMapping(organization)
                .thenCompose(res -> getAll(organization).thenCompose(orders -> doIndexOrder(organization, orders)));
    }

    protected CompletionStage<Boolean> doIndexOrder(final String organization, final List<Order> orders) {
        if (!orders.isEmpty()) {
            return buildIndexableOrder(organization, orders.get(0))
                    .thenCompose(indexableOrder -> searchService.upsert(organization, "orders", indexableOrder))
                    .thenCompose(result -> {
                        orders.remove(0);
                        return doIndexOrder(organization, orders).thenApply(r -> r && result);
                    });
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletionStage<List<OrderComment>> getComments(String organization, final String uuid) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.getComments(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<OrderComment>> addComment(String organization, OrderComment comment) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.addComment(organization, comment));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.OrderComment>> serializeComment(String organization, OrderComment
            comment) {
        if (comment.idUser.isPresent()) {
            return usersService.get(organization, comment.idUser.get())
                    .thenApply(user -> user.map(u -> api.v1.models.OrderComment.serialize(comment, Optional.of(u))));
        } else {
            return CompletableFuture.completedFuture(Optional.of(api.v1.models.OrderComment.serialize(comment, Optional.empty())));
        }
    }

    @Override
    public CompletionStage<Optional<String>> addReportDestination(final String o, ReportDestination rp) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.addReportDestination(o, rp));
    }

    @Override
    public CompletionStage<Boolean> updateReportDestination(String o, ReportDestination rp) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.updateReportDestination(o, rp));
    }

    @Override
    public CompletionStage<Boolean> deleteReportDestination(String o, final String i) {
        return CompletableFuture.supplyAsync(() -> this.ordersRepository.deleteReportDestination(o, i));
    }
}
