package markets;

import agencies.AgenciesService;
import api.v1.models.FullMarket;
import entities.EntitiesService;
import establishments.EstablishmentsService;
import people.PeopleService;
import users.UsersService;
import org.apache.commons.text.similarity.LevenshteinDistance;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

import static java.util.concurrent.CompletableFuture.runAsync;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SimpleMarketsService implements MarketsService {

    protected final MarketsRepository marketsRepository;
    protected final EntitiesService entitiesService;
    protected final EstablishmentsService establishmentsService;
    protected final PeopleService peopleService;
    protected final UsersService usersService;
    protected final AgenciesService agencyService;

    @Inject
    public SimpleMarketsService(
            MarketsRepository marketsRepository,
            EntitiesService entitiesService,
            EstablishmentsService establishmentsService,
            PeopleService peopleService,
            UsersService usersService,
            AgenciesService agencyService
    ) {
        this.marketsRepository = marketsRepository;
        this.entitiesService = entitiesService;
        this.establishmentsService = establishmentsService;
        this.peopleService = peopleService;
        this.usersService = usersService;
        this.agencyService = agencyService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, SimpleMarket market) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.add(organization, market));
    }

    @Override
    public CompletionStage<List<SimpleMarket>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<SimpleMarket>> getFromAccount(String organization, String accountId) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getFromEstablishment(organization, accountId));
    }

    @Override
    public CompletionStage<List<SimpleMarket>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<SimpleMarket>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<SimpleMarket>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<FullMarket> serialize(String organization, SimpleMarket market) {
        FullMarket fullMarket = FullMarket.serialize(market);

        fullMarket.marketEstablishments = this.marketsRepository.getMarketEstablishmentByMarket(market.uuid).stream().map(simpleMarketEstablishment ->
                new api.v1.models.MarketEstablishment(
                        this.establishmentsService.get(organization, simpleMarketEstablishment.establishment)
                                .thenCompose(account -> this.establishmentsService.serialize(organization, account.get()))
                                .toCompletableFuture().join(),
                        simpleMarketEstablishment.role)
        ).collect(Collectors.toList());

        fullMarket.marketPeoples = this.marketsRepository.getMarketPeopleByMarket(market.uuid).stream().map(simpleMarketPeople ->
                new api.v1.models.MarketPeople(
                        this.peopleService.get(organization, simpleMarketPeople.people)
                                .thenCompose(people -> this.peopleService.serialize(organization, people.get()))
                                .toCompletableFuture().join(),
                        simpleMarketPeople.role)
        ).collect(Collectors.toList());

        fullMarket.marketUsers = this.marketsRepository.getMarketUserByMarket(market.uuid).stream().map(simpleMarketUser ->
                new api.v1.models.MarketUser(
                        this.usersService.get(organization, simpleMarketUser.user).toCompletableFuture().join().get(),
                        simpleMarketUser.role)
        ).collect(Collectors.toList());

        fullMarket.bpu = this.marketsRepository.getBpuByMarket(market.uuid);

        fullMarket.agency = this.agencyService.get(organization, market.agency)
                .thenApply(agency -> this.agencyService.serialize(organization, agency.get()).toCompletableFuture().join())
                .toCompletableFuture()
                .join();
        return CompletableFuture.completedFuture(fullMarket);
    }

    @Override
    public CompletionStage<Optional<SimpleMarket>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.delete(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<String>> addMarket(String organization, Market market) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.addMarket(organization, market));
    }

    @Override
    public CompletionStage<Boolean> addContact(MarketPeople marketPeople, String uuid) {
        List<SimpleMarketPeople> marketPeoples = this.getContact(uuid).toCompletableFuture().join();
        marketPeoples = marketPeoples
                .stream()
                .filter(mp -> marketPeople.people.uuid.equals(mp.people) && marketPeople.role.equals(mp.role))
                .collect(Collectors.toList());
        if (marketPeoples.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> this.marketsRepository.addContact(marketPeople, uuid));
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletionStage<List<SimpleMarketPeople>> getContact(String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getMarketPeopleByMarket(uuid));
    }

    @Override
    public CompletionStage<List<SimpleMarketPeople>> getPeopleByRole(String organization, String marketId, String role) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getPeopleByRole(organization, marketId, role));
    }

    @Override
    public CompletionStage<api.v1.models.MarketPeople> serializeMarketPeople(String organization, SimpleMarketPeople marketPeople) {
        return this.peopleService.get(organization, marketPeople.people).thenApply(people ->
                api.v1.models.MarketPeople.serialize(
                        this.peopleService.serialize(organization, people.get()).toCompletableFuture().join(),
                        marketPeople.role
                )
        );
    }

    @Override
    public CompletionStage<Void> updateContact(MarketPeople marketPeople, String uuid, String peopleUuid, String oldRole) {
        List<SimpleMarketPeople> marketPeopleList = this.getContact(uuid).toCompletableFuture().join();
        List<SimpleMarketPeople> mandatoryRole = marketPeopleList.stream()
                .peek(marketPeopleDao -> {
                    if (marketPeopleDao.people.equals(marketPeople.people.uuid)) {
                        marketPeopleDao.role = marketPeople.role;
                    }
                })
                .filter(marketPeopleDao -> MarketPeopleRole.KEY.toString().equalsIgnoreCase(marketPeopleDao.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and people");
        }
        List<SimpleMarketPeople> duplicate = marketPeopleList.stream()
                .peek(marketPeopleDao -> {
                    if (!peopleUuid.equalsIgnoreCase(marketPeople.people.uuid) && marketPeopleDao.people.equalsIgnoreCase(peopleUuid)) {
                        marketPeopleDao.people = marketPeople.people.uuid;
                    }
                }).collect(Collectors.toList());
        Set<SimpleMarketPeople> set = new HashSet<>(duplicate);
        if (set.size() != duplicate.size()) {
            throw new AssertionError("At least one people have more than one role");
        }

        return runAsync(() -> this.marketsRepository.updateContact(marketPeople, uuid, peopleUuid, oldRole));
    }

    @Override
    public CompletionStage<Void> deleteContact(String uuid, String peopleUuid, String role) {
        List<SimpleMarketPeople> marketPeopleList = this.getContact(uuid).toCompletableFuture().join();
        List<SimpleMarketPeople> mandatoryRole = marketPeopleList.stream()
                .filter(marketPeople -> !marketPeople.people.equals(peopleUuid))
                .filter(marketPeople -> MarketPeopleRole.KEY.toString().equalsIgnoreCase(marketPeople.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and people");
        }

        return runAsync(() -> this.marketsRepository.deleteContact(uuid, peopleUuid, role));
    }

    @Override
    public CompletionStage<Boolean> addAccount(MarketEstablishment marketEstablishment, String uuid) {
        List<SimpleMarketEstablishment> marketAccounts = this.getEstablishment(uuid).toCompletableFuture().join();
        marketAccounts = marketAccounts
                .stream()
                .filter(ma -> marketEstablishment.establishment.uuid.equals(ma.establishment) && ma.role.equals(marketEstablishment.role))
                .collect(Collectors.toList());
        if (marketAccounts.isEmpty()) {
            return CompletableFuture.supplyAsync(() -> this.marketsRepository.addEstablishment(marketEstablishment, uuid));
        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletionStage<List<SimpleMarketEstablishment>> getEstablishment(String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getMarketEstablishmentByMarket(uuid));
    }

    @Override
    public CompletionStage<Void> updateAccount(MarketEstablishment marketEstablishment, String uuid, String establishmentUuid, String organization) {
        List<SimpleMarketEstablishment> marketEstablishmentList = this.getEstablishment(uuid).toCompletableFuture().join();
        List<SimpleMarketEstablishment> mandatoryRole = marketEstablishmentList.stream()
                .peek(marketAccountTmp -> {
                    if (marketAccountTmp.establishment.equals(marketEstablishment.establishment.uuid)) {
                        marketAccountTmp.role = marketEstablishment.role;
                    }
                })
                .filter(marketPeopleDao -> MarketEstablishmentRole.CLIENT.toString().equalsIgnoreCase(marketPeopleDao.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and account");
        }
        List<SimpleMarketEstablishment> duplicate = marketEstablishmentList.stream()
                .peek(marketAccountTmp -> {
                    if (!establishmentUuid.equalsIgnoreCase(marketEstablishment.establishment.uuid) && marketAccountTmp.establishment.equalsIgnoreCase(establishmentUuid)) {
                        marketAccountTmp.establishment = marketEstablishment.establishment.uuid;
                    }
                }).collect(Collectors.toList());
        Set<SimpleMarketEstablishment> set = new HashSet<>(duplicate);
        if (set.size() != duplicate.size()) {
            throw new AssertionError("At least one account have more than one role");
        }

        return runAsync(() -> this.marketsRepository.updateEstablishment(marketEstablishment, uuid, establishmentUuid));
    }

    @Override
    public CompletionStage<Void> deleteAccount(String uuid, String establishment, String role) {
        List<SimpleMarketEstablishment> marketAccountList = this.getEstablishment(uuid).toCompletableFuture().join();
        List<SimpleMarketEstablishment> mandatoryRole = marketAccountList.stream()
                .filter(marketAccount -> !marketAccount.establishment.equals(establishment))
                .filter(marketAccount -> MarketEstablishmentRole.CLIENT.toString().equalsIgnoreCase(marketAccount.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and account");
        }

        return runAsync(() -> this.marketsRepository.deleteAccount(uuid, establishment, role));
    }

    @Override
    public CompletionStage<api.v1.models.MarketEstablishment> serializeMarketEstablishment(String organization, SimpleMarketEstablishment marketEstablishement) {
        return this.establishmentsService.get(organization, marketEstablishement.establishment).thenApply(establishment ->
                api.v1.models.MarketEstablishment.serialize(
                        this.establishmentsService.serialize(organization, establishment.get()).toCompletableFuture().join(),
                        marketEstablishement.role
                )
        );
    }

    @Override
    public CompletionStage<Void> addUser(MarketUser marketUser, String uuid) {
        List<SimpleMarketUser> marketUsers = this.getUser(uuid).toCompletableFuture().join();
        marketUsers = marketUsers
                .stream()
                .filter(mu -> marketUser.user.login.equals(mu.user))
                .collect(Collectors.toList());
        if (!marketUsers.isEmpty()) {
            throw new AssertionError("This user is already link to this market");
        }

        return runAsync(() -> this.marketsRepository.addUser(marketUser, uuid));
    }

    @Override
    public CompletionStage<List<SimpleMarketUser>> getUser(String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getMarketUserByMarket(uuid));
    }

    @Override
    public CompletionStage<Void> updateUser(MarketUser marketUser, String uuid, String userUuid, String organization) {
        List<SimpleMarketUser> marketUserList = this.getUser(uuid).toCompletableFuture().join();
        List<SimpleMarketUser> mandatoryRole = marketUserList.stream()
                .peek(marketUserTmp -> {
                    if (marketUserTmp.user.equals(marketUser.user.login)) {
                        marketUserTmp.role = marketUser.role;
                    }
                })
                .filter(marketPeopleDao -> MarketUserRole.COMMERCIAL.toString().equalsIgnoreCase(marketPeopleDao.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and user");
        }
        List<SimpleMarketUser> duplicate = marketUserList.stream()
                .peek(marketUserTmp -> {
                    if (!userUuid.equalsIgnoreCase(marketUser.user.login) && marketUserTmp.user.equalsIgnoreCase(userUuid)) {
                        marketUserTmp.user = marketUser.user.login;
                    }
                }).collect(Collectors.toList());
        Set<SimpleMarketUser> set = new HashSet<>(duplicate);
        if (set.size() != duplicate.size()) {
            throw new AssertionError("At least one user have more than one role");
        }

        return runAsync(() -> this.marketsRepository.updateUser(marketUser, uuid, userUuid));
    }

    @Override
    public CompletionStage<Void> deleteUser(String uuid, String userLogin) {
        List<SimpleMarketUser> marketUserList = this.getUser(uuid).toCompletableFuture().join();
        List<SimpleMarketUser> mandatoryRole = marketUserList.stream()
                .filter(marketUser -> !marketUser.user.equals(userLogin))
                .filter(marketUser -> MarketUserRole.COMMERCIAL.toString().equalsIgnoreCase(marketUser.role))
                .collect(Collectors.toList());
        if (mandatoryRole.isEmpty()) {
            throw new AssertionError("No more required relation between market and user");
        }

        return runAsync(() -> this.marketsRepository.deleteUser(uuid, userLogin));
    }

    @Override
    public CompletionStage<api.v1.models.MarketUser> serializeMarketUser(String organization, SimpleMarketUser marketUser) {
        return this.usersService.get(organization, marketUser.user).thenApply(user ->
                api.v1.models.MarketUser.serialize(user.get(), marketUser.role)
        );
    }

    @Override
    public CompletionStage<Void> updateMarket(Market market) {
        return runAsync(() -> this.marketsRepository.updateMarket(market));
    }

    @Override
    public CompletionStage<Optional<String>> addReferenceIfNotExist(String uuid, BpuReference reference) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.addReferenceIfNotExist(uuid, reference));
    }

    @Override
    public CompletionStage<List<BpuReference>> getReferences(String uuid, String reference) {
        return CompletableFuture.supplyAsync(() -> {
            List<BpuReference> references = marketsRepository.getReferences(uuid, reference).stream().filter(r -> r.reference.isPresent()).collect(Collectors.toList());
            references.sort((o1, o2) -> {
                Integer o1value = LevenshteinDistance.getDefaultInstance().apply(reference, o1.reference.get());
                Integer o2value = LevenshteinDistance.getDefaultInstance().apply(reference, o2.reference.get());
                return o1value - o2value;
            });
            return references;
        });
    }

    @Override
    public CompletionStage<List<BpuReference>> getReferencesFromDesignation(String uuid, String designation) {
        return CompletableFuture.supplyAsync(() -> {
            List<BpuReference> references = marketsRepository.getReferencesFromDesignation(uuid, designation).stream().filter(r -> r.designation.isPresent()).collect(Collectors.toList());
            references.sort((o1, o2) -> {
                Integer o1value = LevenshteinDistance.getDefaultInstance().apply(designation, o1.designation.get());
                Integer o2value = LevenshteinDistance.getDefaultInstance().apply(designation, o2.designation.get());
                return o1value - o2value;
            });
            return references;
        });
    }

    @Override
    public CompletionStage<Optional<Bpu>> getBpu(String uuid) {
        return supplyAsync(() -> this.marketsRepository.getBpu(uuid));
    }

    @Override
    public CompletionStage<Bpu> addBpu(String organization, Bpu bpu) {
        return supplyAsync(() ->
                this.marketsRepository
                        .addBpu(organization, bpu)
                        .flatMap(this.marketsRepository::getBpu)
                        .orElse(null)
        );
    }

    @Override
    public CompletionStage<Void> deleteBpu(String uuid) {
        return runAsync(() -> this.marketsRepository.deleteBpu(uuid));
    }

    @Override
    public CompletionStage<List<MarketComment>> getComments(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.getComments(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<MarketComment>> addComment(String organization, MarketComment marketComment) {
        return CompletableFuture.supplyAsync(() -> this.marketsRepository.addComment(organization, marketComment));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.MarketComment>> serializeComment(String organization, MarketComment comment) {
        if (comment.idUser.isPresent()) {
            return usersService.get(organization, comment.idUser.get())
                    .thenApply(user -> user.map(u -> api.v1.models.MarketComment.serialize(comment, Optional.of(u))));
        } else {
            return CompletableFuture.completedFuture(Optional.of(api.v1.models.MarketComment.serialize(comment, Optional.empty())));
        }
    }
}
