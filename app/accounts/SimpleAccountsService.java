package accounts;

import addresses.AddressesService;
import com.sun.org.apache.xpath.internal.operations.Bool;
import core.EventType;
import core.search.AbstractSearchService;
import core.search.Pageable;
import core.search.PaginatedResult;
import core.search.SearchService;
import entities.EntitiesService;
import groups.Group;
import groups.GroupsRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import people.PeopleAddressRole;
import people.PeopleService;
import users.UsersService;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.allOf;
import static java.util.concurrent.CompletableFuture.supplyAsync;

public class SimpleAccountsService extends AbstractSearchService implements AccountsService {
    protected final Logger logger = LoggerFactory.getLogger(SimpleAccountsService.class);

    protected final AccountsRepository accountsRepository;
    protected final GroupsRepository groupsRepository;
    protected final PeopleService peopleService;
    protected final EntitiesService entitiesService;
    protected final UsersService usersService;
    protected final SearchService searchService;
    protected final AddressesService addressesService;

    @Inject
    public SimpleAccountsService(AccountsRepository accountsRepository, GroupsRepository groupsRepository, PeopleService peopleService,
                                 EntitiesService entitiesService, UsersService usersService, SearchService searchService, AddressesService addressesService) {
        super(searchService);
        this.accountsRepository = accountsRepository;
        this.groupsRepository = groupsRepository;
        this.peopleService = peopleService;
        this.entitiesService = entitiesService;
        this.usersService = usersService;
        this.searchService = searchService;
        this.addressesService = addressesService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Account account, Optional<String> login) {
        if (account.entity.isPresent()) {
            buildIndexableProfessional(organization, account).thenCompose(indexableAccount -> searchService.upsert(organization, "professionals", indexableAccount));
        } else {
            buildIndexableIndividual(organization, account).thenCompose(indexableAccount -> searchService.upsert(organization, "individuals", indexableAccount));
        }
        return CompletableFuture.supplyAsync(() -> {
            final Optional<String> accounId = this.accountsRepository.add(organization, account);
            if (accounId.isPresent() && login.isPresent()) {
                this.addComment(organization, new AccountComment(Optional.empty(), accounId.get(), login, "<b>status : </b>Compte validé", new Date(), EventType.STATUS));
            }
            return accounId;
        });
    }

    @Override
    public CompletionStage<Optional<Account>> update(String organization, Account account, Optional<List<String>> groups) {
        return supplyAsync(() -> this.accountsRepository.update(organization, account)).thenCompose(updated -> {
            if (updated.isPresent() && groups.isPresent()) {
                List<Group> accountGroups = this.groupsRepository.getFromAccount(organization, account.uuid);
                List<String> groupsId = new ArrayList<>();
                for (Group group : accountGroups) {
                    groupsId.add(group.uuid);
                }
                for (String newGroup : groups.get()) {
                    Optional<Group> existingGroup = this.groupsRepository.get(organization, newGroup);
                    if (existingGroup.isPresent() && !groupsId.contains(newGroup)) {
                        this.accountsRepository.linkAccountGroup(account.uuid, newGroup);
                        groupsId.remove(newGroup);
                    } else if (existingGroup.isPresent()) {
                        groupsId.remove(newGroup);
                    }
                }
                for (String oldGroup : groupsId) {
                    this.accountsRepository.unlinkAccountGroup(account.uuid, oldGroup);
                }
            }
            CompletionStage<Boolean> res;

            if (updated.get().entity.isPresent()) {
                res = buildIndexableProfessional(organization, updated.get()).thenCompose(indexableAccount -> searchService.upsert(organization, "professionals", indexableAccount));
            } else {
                res = buildIndexableIndividual(organization, updated.get()).thenCompose(indexableAccount -> searchService.upsert(organization, "individuals", indexableAccount));
            }

            return res.thenApply(e -> updated).toCompletableFuture();
        });
    }

    @Override
    public CompletionStage<Optional<Account>> get(String organization, String uuid) {
        return supplyAsync(() -> this.accountsRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<Account>> getFromEntity(String organization, String entityId) {
        return supplyAsync(() -> this.accountsRepository.getFromEntity(organization, entityId));
    }

    @Override
    public CompletionStage<List<Account>> getAll(String organization) {
        return supplyAsync(() -> this.accountsRepository.getAll(organization));
    }

    public CompletionStage<List<Account>> getAllNotDeleted(String organization) {
        return supplyAsync(() -> this.accountsRepository.getAllNotDeleted(organization));
    }

    @Override
    public CompletionStage<List<Account>> getPage(String organization, Integer offset, Integer length) {
        return supplyAsync(() -> this.accountsRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Account>> getAllIndividuals(String organization) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getAllIndividuals(organization));
    }

    @Override
    public CompletionStage<List<Account>> getPageIndividuals(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getPageIndividuals(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Account>> getAllProfessionals(String organization) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getAllProfessionals(organization));
    }

    @Override
    public CompletionStage<List<Account>> getPageProfessionals(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getPageProfessionals(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Account>> getAdministrativeValidatorsForExport(String organization) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getAdministrativeValidatorsForExport(organization));
    }

    @Override
    public CompletionStage<List<Account>> suggest(String organization, String pattern) {
        return supplyAsync(() -> this.accountsRepository.suggest(organization, pattern));
    }

    @Override
    public CompletionStage<List<Account>> search(String organization, String pattern) {
        return supplyAsync(() -> this.accountsRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Account>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return supplyAsync(() -> this.accountsRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Boolean> delete(String organization, String uuid) {
        return supplyAsync(() -> this.accountsRepository.deleteOne(uuid))
                .thenCompose(res -> searchService.delete(organization, "professionals", uuid).thenApply(e -> res))
                .thenCompose(res -> searchService.delete(organization, "individuals", uuid).thenApply(e -> res));
    }

    @Override
    public CompletionStage<api.v1.models.Account> serialize(String organization, Account account) {
        return usersService.get(organization, account.commercial).thenCompose(commercial ->
                peopleService.get(organization, account.contact).thenCompose(contact -> peopleService.serialize(organization, contact.get())).thenCompose(finalContact -> {
                    if (account.entity.isPresent()) {
                        return entitiesService.get(organization, account.entity.get()).thenCompose(entity -> entitiesService.serialize(organization, entity.get())).thenApply(finalEntity -> api.v1.models.Account.serialize(account, commercial.get(), finalContact, Optional.of(finalEntity)));
                    } else {
                        return CompletableFuture.completedFuture(api.v1.models.Account.serialize(account, commercial.get(), finalContact, Optional.empty()));
                    }
                }));
    }

    @Override
    public CompletionStage<Boolean> changeStatusWhenAddingEstablishment(String organization, String establishment) {
        return supplyAsync(() -> this.accountsRepository.changeStatusWhenAddingEstablishment(organization, establishment));
    }

    public CompletionStage<Optional<IndexableProfessional>> buildIndexableProfessional(final String organization, final String accountId) {
        return get(organization, accountId).thenCompose(maybeAccount -> {
            if (maybeAccount.isPresent()) {
                return buildIndexableProfessional(organization, maybeAccount.get()).thenApply(Optional::of);
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableProfessional> buildIndexableProfessional(String organization, Account account) {
        return this.peopleService.get(organization, account.contact).thenCompose(people -> {
            if (people.isPresent()) {
                return this.entitiesService.get(organization, account.entity.get()).thenCompose(entity -> {
                    if (entity.isPresent()) {
                        IndexableAccountPeople accountPeople = new IndexableAccountPeople(people.get().uuid, people.get().lastname.toUpperCase() + " " + people.get().firstname, people.get().mobilePhone.orElse(""));
                        IndexableAccountEntity accountEntity = new IndexableAccountEntity(entity.get().uuid, entity.get().name, entity.get().siren);
                        return this.usersService.get(organization, account.commercial).thenCompose(commercial -> {
                            Optional<AccountUserIndexable> accountUser = commercial.map(user -> new AccountUserIndexable(user.login, user.first_name + " " + user.last_name));
                            if (entity.get().mainAddress.isPresent()) {
                                return this.addressesService.get(organization, entity.get().mainAddress.get()).thenApply(address -> {
                                    if (address.isPresent()) {
                                        String addressName = address.get().address1.isPresent() && address.get().postCode.isPresent() && address.get().city.isPresent()
                                                ? address.get().address1.get() + (address.get().address2.map(s -> ", " + s + ", ").orElse(", ")) + address.get().postCode.get() + " " + address.get().city.get()
                                                : address.get().gpsCoordinates.orElseGet(() -> (address.get().inseeCoordinates.orElse("")));
                                        return new IndexableProfessional(account.uuid, entity.get().name, account.category, account.type, account.state, new Timestamp(account.created.getTime()).toString(), accountPeople, Optional.of(addressName), accountUser, accountEntity);
                                    } else {
                                        return new IndexableProfessional(account.uuid, entity.get().name, account.category, account.type, account.state, new Timestamp(account.created.getTime()).toString(), accountPeople, Optional.empty(), accountUser, accountEntity);
                                    }
                                });
                            } else {
                                return CompletableFuture.completedFuture(new IndexableProfessional(account.uuid, entity.get().name, account.category, account.type, account.state, new Timestamp(account.created.getTime()).toString(), accountPeople, Optional.empty(), accountUser, accountEntity));
                            }
                        });
                    } else {
                        throw new RuntimeException("Failed to get entity " + account.entity.get() + " from account " + account.uuid);
                    }
                });
            } else {
                throw new RuntimeException("Failed to get people " + account.contact + " from account " + account.uuid);
            }
        });

    }

    public CompletionStage<Optional<IndexableIndividual>> buildIndexableIndividual(final String organization, final String accountId) {
        return get(organization, accountId).thenCompose(maybeAccount -> {
            if (maybeAccount.isPresent()) {
                return buildIndexableIndividual(organization, maybeAccount.get()).thenApply(Optional::of);
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableIndividual> buildIndexableIndividual(String organization, Account account) {
        return this.peopleService.get(organization, account.contact).thenCompose(people -> {
            if (people.isPresent()) {
                String peopleName = people.get().lastname.toUpperCase() + " " + people.get().firstname;
                IndexableAccountPeople accountPeople = new IndexableAccountPeople(people.get().uuid, peopleName, people.get().mobilePhone.orElse(""));
                return this.usersService.get(organization, account.commercial).thenCompose(commercial -> {
                    Optional<AccountUserIndexable> accountUser = commercial.map(user -> new AccountUserIndexable(user.login, user.first_name + " " + user.last_name));
                    return this.peopleService.getAddressesByRole(organization, people.get().uuid, PeopleAddressRole.MAIN.toString()).thenCompose(addressWithRoles -> {
                        if (!addressWithRoles.isEmpty()) {
                            return this.addressesService.get(organization, addressWithRoles.get(0).address).thenApply(address -> {
                                if (address.isPresent()) {
                                    String addressName = address.get().address1.isPresent() && address.get().postCode.isPresent() && address.get().city.isPresent()
                                            ? address.get().address1.get() + (address.get().address2.map(s -> ", " + s + ", ").orElse(", ")) + address.get().postCode.get() + " " + address.get().city.get()
                                            : address.get().gpsCoordinates.orElseGet(() -> (address.get().inseeCoordinates.orElse("")));
                                    return new IndexableIndividual(account.uuid, peopleName, account.category, account.type, account.state, account.created, accountPeople, Optional.of(addressName), accountUser);
                                } else {
                                    return new IndexableIndividual(account.uuid, peopleName, account.category, account.type, account.state, account.created, accountPeople, Optional.empty(), accountUser);
                                }
                            });
                        } else {
                            return CompletableFuture.completedFuture(new IndexableIndividual(account.uuid, peopleName, account.category, account.type, account.state, account.created, accountPeople, Optional.empty(), accountUser));
                        }
                    });
                });
            } else {
                throw new RuntimeException("Failed to get people " + account.contact + " from account " + account.uuid);
            }
        });
    }

    public CompletionStage<Boolean> setMapping(String organization) {
        Map<String, Map<String, String>> mapping = new HashMap<>();
        Map<String, String> created = new HashMap<>();
        created.put("type", "date");
        created.put("format", "epoch_millis");
        mapping.put("created", created);
        return searchService.setMapping(organization, "individuals", mapping);
    }

    public CompletionStage<Boolean> reindex(final String organization) {
        return setMapping(organization)
                .thenCompose(res -> getAllNotDeleted(organization).thenCompose(accounts -> doIndexAccount(organization, accounts)));
    }

    protected CompletionStage<Boolean> doIndexAccount(final String organization, final List<Account> accounts) {
        if (!accounts.isEmpty()) {
            if (accounts.get(0).entity.isPresent()) {
                return buildIndexableProfessional(organization, accounts.get(0))
                        .thenCompose(indexableProfessional -> searchService.upsert(organization, "professionals", indexableProfessional))
                        .thenCompose(result -> {
                            accounts.remove(0);
                            return doIndexAccount(organization, accounts).thenApply(r -> r && result);
                        });
            } else {
                return buildIndexableIndividual(organization, accounts.get(0))
                        .thenCompose(indexableIndividual -> searchService.upsert(organization, "individuals", indexableIndividual))
                        .thenCompose(result -> {
                            accounts.remove(0);
                            return doIndexAccount(organization, accounts).thenApply(r -> r && result);
                        });
            }

        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableProfessional>>> getProfessionalOverviews(final String organization,
                                                                                                  final Pageable pageable) {
        return this.getElasticResult(IndexableProfessional.class, "professionals", organization, pageable);
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableIndividual>>> getIndividualOverviews(final String organization,
                                                                                              final Pageable pageable) {
        return this.getElasticResult(IndexableIndividual.class, "individuals", organization, pageable);
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableAccount>>> getAccountOverviews(final String organization,
                                                                                        final Pageable pageable) {
        return this.getElasticResult(IndexableProfessional.class, new String[]{"adx-individuals", "adx-professionals"}, organization, pageable);
    }

    @Override
    public CompletionStage<List<AccountComment>> getComments(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.getComments(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<AccountComment>> addComment(String organization, AccountComment comment) {
        return CompletableFuture.supplyAsync(() -> this.accountsRepository.addComment(organization, comment));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.AccountComment>> serializeComment(String organization, AccountComment comment) {
        if (comment.idUser.isPresent()) {
            return usersService.get(organization, comment.idUser.get())
                    .thenApply(users -> users.map(user -> api.v1.models.AccountComment.serialize(comment, Optional.of(user))));
        } else {
            return CompletableFuture.completedFuture(Optional.of(api.v1.models.AccountComment.serialize(comment, Optional.empty())));
        }

    }
}
