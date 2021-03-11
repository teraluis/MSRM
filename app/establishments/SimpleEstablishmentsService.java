package establishments;

import accounts.AccountComment;
import accounts.AccountsService;
import activities.ActivitiesService;
import addresses.Address;
import addresses.AddressWithRole;
import addresses.AddressesService;
import agencies.AgenciesService;
import core.search.Pageable;
import core.search.AbstractSearchService;
import core.CompletableFutureUtils;
import core.EventType;
import core.search.SearchService;
import entities.EntitiesService;
import nl.garvelink.iban.IBAN;
import nl.garvelink.iban.Modulo97;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import people.People;
import people.PeopleService;
import people.PeopleWithRole;
import users.UsersService;
import core.search.PaginatedResult;


import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SimpleEstablishmentsService extends AbstractSearchService<IndexableEstablishment> implements EstablishmentsService {
    protected final Logger logger = LoggerFactory.getLogger(SimpleEstablishmentsService.class);

    protected final EstablishmentsRepository establishmentsRepository;
    protected final EntitiesService entitiesService;
    protected final AddressesService addressesService;
    protected final PeopleService peopleService;
    protected final ActivitiesService activitiesService;
    protected final AccountsService accountsService;
    protected final SearchService searchService;
    protected final UsersService usersService;
    protected final AgenciesService agenciesService;

    @Inject
    public SimpleEstablishmentsService(EstablishmentsRepository establishmentsRepository, EntitiesService entitiesService, AddressesService addressesService, PeopleService peopleService, ActivitiesService activitiesService, AccountsService accountsService, SearchService searchService, UsersService usersService, AgenciesService agenciesService) {
        super(IndexableEstablishment.class, searchService);
        this.establishmentsRepository = establishmentsRepository;
        this.entitiesService = entitiesService;
        this.addressesService = addressesService;
        this.peopleService = peopleService;
        this.activitiesService = activitiesService;
        this.accountsService = accountsService;
        this.searchService = searchService;
        this.usersService = usersService;
        this.agenciesService = agenciesService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Establishment establishment, Optional<String> login) {
        buildIndexableEstablishment(organization, establishment).thenCompose(indexableEstablishment -> searchService.upsert(organization, "establishments", indexableEstablishment));
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.add(organization, establishment))
                // Change account status after adding a new establishment
                .thenCompose(newEstablishment -> accountsService.changeStatusWhenAddingEstablishment(organization, establishment.uuid)
                        // Retrieve account for comments
                        .thenCompose(done -> accountsService.getFromEntity(organization, establishment.entity)
                                .thenApply(account -> {
                                    // Add comment to the account
                                    String message = "<b>ajout :</b>Établissement " + establishment.name + " ajouté au compte" + account.get().reference + "</b>";
                                    this.accountsService.addComment(organization, new AccountComment(Optional.empty(), account.get().uuid, login, message, new Date(), EventType.MODIFICATION));
                                    // Add comment to this establishment
                                    message = "<b>Statut : </b>L'établissement \"" + establishment.name.toUpperCase() + "\" a été créé, contact : " + login.orElse("inconnu");
                                    this.establishmentsRepository.addComment(organization, new EstablishmentComment(Optional.empty(), newEstablishment.get(), login, message, new Date(), EventType.STATUS));
                                    return newEstablishment;
                                })));
    }

    @Override
    public CompletionStage<Optional<Establishment>> update(String organization, Establishment establishment, Optional<String> login) {
        buildIndexableEstablishment(organization, establishment).thenCompose(indexableEstablishment -> searchService.upsert(organization, "establishments", indexableEstablishment));
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.get(organization, establishment.getId()))
                .thenCompose(oldEstablishment -> CompletableFuture.supplyAsync(() -> this.establishmentsRepository.update(organization, establishment))
                        .thenCompose(updated -> this.addEvents(organization, oldEstablishment.get(), updated.get(), login.orElse("calypso"))
                                .thenApply(unused -> updated)));
    }

    public CompletionStage<Boolean> addEvents(String organization, Establishment oldEstablishment, Establishment updated, String login) {
        return CompletableFuture.supplyAsync(() -> {
            final boolean name = oldEstablishment.name.equals(updated.name);
            final boolean email = oldEstablishment.mail.map(mail -> updated.mail.isPresent() && updated.mail.get().equals(mail)).orElse(!updated.mail.isPresent());
            final boolean phoneClient = oldEstablishment.phone.map(phone -> updated.phone.isPresent() && updated.phone.get().equals(phone)).orElse(!updated.phone.isPresent());
            final boolean corporateName = oldEstablishment.corporateName.equals(updated.corporateName);
            final boolean NIC = oldEstablishment.siret.equals(updated.siret);

            List<String> messages = new ArrayList<>();
            if (!corporateName) {
                messages.add(this.generateUpdateMessage(login, "Raison sociale", oldEstablishment.corporateName, updated.corporateName));
            }
            if (!phoneClient) {
                if (!oldEstablishment.phone.isPresent()) {
                    messages.add("Le champ téléphone a été ajouté par " + login + " sa valeur est : " + updated.phone.get());
                } else {
                    messages.add(this.generateUpdateMessage(login, "TELEPHONE", oldEstablishment.phone.orElse(""), updated.phone.orElse("")));
                }
            }
            if (!email) {
                if (!oldEstablishment.mail.isPresent()) {
                    messages.add("Le champ email a été ajouté par " + login + " sa valeur est : " + updated.mail.get());
                } else {
                    messages.add(this.generateUpdateMessage(login, "MAIL", oldEstablishment.mail.orElse(""), updated.mail.orElse("")));
                }
            }
            if (!NIC) {
                messages.add(this.generateUpdateMessage(login, "SIRET", oldEstablishment.siret, updated.siret));
            }
            if (!name) {
                messages.add(this.generateUpdateMessage(login, "NOM", oldEstablishment.name, updated.name));
            }
            List<Boolean> results = messages.stream().map(message -> {
                final Optional<EstablishmentComment> comment =
                        this.establishmentsRepository.addComment(organization, new EstablishmentComment(Optional.empty(), updated.uuid, Optional.of(login), message, new Date(), EventType.MODIFICATION));
                if (!comment.isPresent()) {
                    logger.error("A problem occurs while creating comment for establishment : " + message);
                    return false;
                } else {
                    return true;
                }
            }).collect(Collectors.toList());
            return results.stream().allMatch(r -> r);
        });
    }

    public String generateUpdateMessage(String name, String field, String oldervalue, String newvalue) {
        return "Le champ " + field + " a été modifié. Valeur précédente : " + oldervalue + ", nouvelle valeur : " + newvalue + " contact: " + name;
    }

    @Override
    public CompletionStage<Optional<Establishment>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<Establishment>> getFromSiret(String organization, String siret) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getFromSiret(organization, siret));
    }

    @Override
    public CompletionStage<List<Establishment>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Establishment>> getFromEntity(String organization, String entity) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getFromEntity(organization, entity));
    }

    @Override
    public CompletionStage<List<Establishment>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<String> getSageCode(String organization) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getSageCode(organization));
    }

    @Override
    public CompletionStage<List<Establishment>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Establishment>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<api.v1.models.Establishment> serialize(String organization, Establishment establishment) {
            return this.agenciesService.get(organization, establishment.agency)
                    .thenCompose(agency -> agenciesService.serialize(organization, agency.get())
                            .thenCompose(finalAgency -> getAddressesByRole(organization, establishment.uuid, EstablishmentAddressRole.MAIN.toString())
                                    .thenCompose(addresses -> {
                                        if (addresses.size() > 0) {
                                            return addressesService.get(organization, addresses.get(0).address)
                                                    .thenApply(address -> Optional.of(address.get().serialize()))
                                                    .toCompletableFuture();
                                        } else {
                                            return CompletableFuture.completedFuture(Optional.empty());
                                        }
                                    })
                                    .thenCompose(address -> getPeopleByRole(organization, establishment.uuid, EstablishmentPeopleRole.MAIN.toString())
                                            .thenCompose(people -> {
                                                if (people.size() > 0) {
                                                    return peopleService.get(organization, people.get(0).people)
                                                            .thenCompose(person -> peopleService.serialize(organization, person.get()))
                                                            .thenApply(Optional::of)
                                                            .toCompletableFuture();
                                                } else {
                                                    return CompletableFuture.completedFuture(Optional.empty());
                                                }
                                            })
                                            .thenCompose(contact -> {
                                                if (establishment.activity.isPresent()) {
                                                    return activitiesService.get(organization, establishment.activity.get())
                                                            .thenApply(activity -> api.v1.models.Establishment.serialize(establishment, address, contact, Optional.of(api.v1.models.Activity.serialize(activity.get())), finalAgency));
                                                }
                                                return CompletableFuture.completedFuture(api.v1.models.Establishment.serialize(establishment, address, contact, Optional.empty(), finalAgency));
                                            })
                                    )));

    }

    @Override
    public CompletionStage<api.v1.models.FullEstablishment> serializeFull(String organization, Establishment establishment, Boolean hasOrders) {
        return serialize(organization, establishment)
                .thenCompose(finalEstablishment -> accountsService.getFromEntity(organization, establishment.entity)
                        .thenCompose(mainAccount -> accountsService.serialize(organization, mainAccount.get())
                                .thenCompose(finalAccount -> getPeople(organization, establishment.uuid)
                                        .thenCompose(peopleWithRoles -> CompletableFutureUtils.sequence(peopleWithRoles.stream().map(peopleWithRole -> peopleService.get(organization, peopleWithRole.people)
                                                .thenCompose(people -> peopleService.serialize(organization, people.get()))
                                                .thenApply(serializedPeople -> new core.models.PeopleWithRole(serializedPeople, peopleWithRole.role))
                                                .toCompletableFuture())
                                                .collect(Collectors.toList()))
                                        )
                                        .thenCompose(finalPeople -> getAddresses(organization, establishment.uuid)
                                                .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address)
                                                        .thenApply(address -> new core.models.AddressWithRole(address.get().serialize(), addressWithRole.role))
                                                        .toCompletableFuture())
                                                        .collect(Collectors.toList()))
                                                )
                                                .thenCompose(finalAddresses -> getDelegates(organization, establishment.uuid)
                                                        .thenCompose(delegateWithRoles -> CompletableFutureUtils.sequence(delegateWithRoles.stream().map(delegateWithRole -> this.get(organization, delegateWithRole.establishment)
                                                                .thenCompose(delegate -> this.serialize(organization, delegate.get()))
                                                                .thenApply(serializedDelegate -> api.v1.models.EstablishmentWithRole.serialize(serializedDelegate, delegateWithRole.role))
                                                                .toCompletableFuture())
                                                                .collect(Collectors.toList()))
                                                        )
                                                        .thenApply(finalDelegates -> new api.v1.models.FullEstablishment(finalEstablishment, finalAccount, finalAddresses, finalDelegates, finalPeople, hasOrders)))))));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.delete(organization, uuid));
    }

    @Override
    public CompletionStage<Boolean> setClientExported(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.setClientExported(organization, establishmentId));
    }

    @Override
    public CompletionStage<Boolean> setValidatorExported(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.setValidatorExported(organization, establishmentId));
    }

    @Override
    public CompletionStage<Boolean> setClientUpToDate(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.setClientUpToDate(organization, establishmentId));
    }

    @Override
    public CompletionStage<Boolean> setValidatorUpToDate(String organization, String establishmentId) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.setValidatorUpToDate(organization, establishmentId));
    }

    @Override
    public CompletionStage<List<EstablishmentWithRole>> getDelegatesByRole(String organization, String uuid, String role) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getDelegatesByRole(organization, uuid, role));
    }

    @Override
    public CompletionStage<List<EstablishmentWithRole>> getDelegates(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getDelegates(organization, uuid));
    }

    @Override
    public CompletionStage<List<AddressWithRole>> getAddressesByRole(String organization, String uuid, String role) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getAddressesByRole(organization, uuid, role));
    }

    @Override
    public CompletionStage<List<AddressWithRole>> getAddresses(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getAddresses(organization, uuid));
    }

    @Override
    public CompletionStage<List<PeopleWithRole>> getPeopleByRole(String organization, String uuid, String role) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getPeopleByRole(organization, uuid, role));
    }

    @Override
    public CompletionStage<List<PeopleWithRole>> getPeople(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getPeople(organization, uuid));
    }

    @Override
    public CompletionStage<Boolean> addDelegate(String organization, String establishmentId, String delegateId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.addDelegate(establishmentId, delegateId, role))
                .thenApply(created -> {
                    if (created && login.isPresent()) {
                        this.establishmentsRepository.addComment(organization, new EstablishmentComment(Optional.empty(), establishmentId, login, "<b>ajout : </b>Un tiers a été ajouté ", new Date(), EventType.MODIFICATION));
                        accountsService.addComment(organization, new AccountComment(Optional.empty(), delegateId, login, "<b>ajout : </b>Un établisement a été ajoué", new Date(), EventType.MODIFICATION));
                    }
                    return created;
                });
    }

    @Override
    public CompletionStage<Boolean> addAddress(String organization, String establishmentId, String addressId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.addAddress(establishmentId, addressId, role))
                .thenApply(created -> {
                    this.establishmentsRepository.addComment(organization,
                            new EstablishmentComment(Optional.empty(), establishmentId, login, "Une adresse a été ajoutée, contact : " + login.orElse("inconnu"), new Date(), EventType.MODIFICATION));
                    return created;
                });
    }

    @Override
    public CompletionStage<Boolean> addPeople(String organization, String establishmentId, String peopleId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.addPeople(establishmentId, peopleId, role))
                .thenApply(created -> {
                    this.establishmentsRepository.addComment(organization,
                            new EstablishmentComment(Optional.empty(), establishmentId, login, "Un contact a été ajouté, par : " + login.orElse("inconnu"), new Date(), EventType.MODIFICATION));
                    return created;
                });
    }

    @Override
    public CompletionStage<Boolean> removeDelegate(String organization, String establishmentId, String delegateId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.removeDelegate(establishmentId, delegateId, role))
                .thenApply(deleted -> {
                    this.establishmentsRepository.addComment(organization,
                            new EstablishmentComment(Optional.empty(), establishmentId, login, "Un tiers a été supprimé, par : " + login.orElse("inconnu"), new Date(), EventType.MODIFICATION));
                    return deleted;
                });
    }

    @Override
    public CompletionStage<Boolean> removeAddress(String organization, String establishmentId, String addressId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.removeAddress(establishmentId, addressId, role))
                .thenApply(created -> {
                    this.establishmentsRepository.addComment(organization,
                            new EstablishmentComment(Optional.empty(), establishmentId, login, "Une adresse a été supprimée, par : " + login.orElse("inconnu"), new Date(), EventType.MODIFICATION));
                    return created;
                });
    }

    @Override
    public CompletionStage<Boolean> removePeople(String organization, String establishmentId, String peopleId, String role, Optional<String> login) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.removePeople(establishmentId, peopleId, role))
                .thenApply(created -> {
                    this.establishmentsRepository.addComment(organization,
                            new EstablishmentComment(Optional.empty(), establishmentId, login, "Un contact a été supprimé, par : " + login.orElse("inconnu"), new Date(), EventType.MODIFICATION));
                    return created;
                });
    }

    public CompletionStage<Optional<IndexableEstablishment>> buildIndexableEstablishment(final String organization, final String establishmentId) {
        return get(organization, establishmentId).thenCompose(maybeEstablishment -> {
            if (maybeEstablishment.isPresent()) {
                return buildIndexableEstablishment(organization, maybeEstablishment.get()).thenApply(x -> Optional.of(x));
            } else {
                return CompletableFuture.completedFuture(Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableEstablishment> buildIndexableEstablishment(String organization, Establishment establishment) {
        return this.getPeopleByRole(organization, establishment.uuid, EstablishmentPeopleRole.MAIN.toString()).thenCompose(peopleWithRoles -> {
            if (peopleWithRoles.size() > 0) {
                return this.peopleService.get(organization, peopleWithRoles.get(0).people)
                        .thenCompose(people -> this.buildIndexableEstablishmentWithPeople(organization, establishment, people));
            } else {
                return this.buildIndexableEstablishmentWithPeople(organization, establishment, Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableEstablishment> buildIndexableEstablishmentWithPeople(String organization, Establishment establishment, Optional<People> people) {
        return this.getAddressesByRole(organization, establishment.uuid, EstablishmentAddressRole.MAIN.toString()).thenCompose(addressWithRoles -> {
            if (addressWithRoles.size() > 0) {
                return this.addressesService.get(organization, addressWithRoles.get(0).address)
                        .thenCompose(address -> this.buildIndexableEstablishmentWithAddress(organization, establishment, people, address));
            } else {
                return this.buildIndexableEstablishmentWithAddress(organization, establishment, people, Optional.empty());
            }
        });
    }

    public CompletionStage<IndexableEstablishment> buildIndexableEstablishmentWithAddress(String organization, Establishment establishment, Optional<People> people, Optional<Address> address) {
        Optional<IndexableEstablishment.EstablishmentPeople> establishmentPeople = people.map(value -> new IndexableEstablishment.EstablishmentPeople(value.uuid, value.lastname.toUpperCase() + " " + value.firstname, value.mobilePhone.orElse("")));
        Optional<String> addressName;
        if (address.isPresent()) {
            if (address.get().address1.isPresent() && address.get().postCode.isPresent() && address.get().city.isPresent())
                addressName = Optional.of(address.get().address1.get() + ", " + (address.get().address2.map(s -> s + ", ").orElse("")) + address.get().postCode.get() + " " + address.get().city.get());
            else
                addressName = address.get().gpsCoordinates.isPresent() ? address.get().gpsCoordinates : address.get().inseeCoordinates;
        } else {
            addressName = Optional.empty();
        }
        return entitiesService.get(organization, establishment.entity).thenCompose(entity -> {
            if (entity.isPresent()) {
                IndexableEstablishment.EstablishmentEntity establishmentEntity = new IndexableEstablishment.EstablishmentEntity(entity.get().uuid, entity.get().name);
                return accountsService.getFromEntity(organization, entity.get().uuid).thenCompose(account -> {
                    EstablishmentAccount establishmentAccount = account.map(value -> new EstablishmentAccount(value.uuid, value.state.orElse(""), value.category)).orElseGet(EstablishmentAccount::new);

                    if (establishment.activity.isPresent()) {
                        return activitiesService.get(organization, establishment.activity.get())
                                .thenApply(activity -> new IndexableEstablishment(
                                        establishment.uuid,
                                        establishment.name,
                                        establishment.corporateName,
                                        establishment.siret,
                                        new Timestamp(establishment.created.getTime()).toString(),
                                        establishmentEntity,
                                        establishmentPeople,
                                        establishment.description,
                                        activity.map(value -> value.name),
                                        addressName,
                                        establishment.phone,
                                        establishment.mail,
                                        establishmentAccount
                                ));
                    } else {
                        return CompletableFuture.completedFuture(new IndexableEstablishment(
                                establishment.uuid,
                                establishment.name,
                                establishment.corporateName,
                                establishment.siret,
                                new Timestamp(establishment.created.getTime()).toString(),
                                establishmentEntity,
                                establishmentPeople,
                                establishment.description,
                                Optional.empty(),
                                addressName,
                                establishment.phone,
                                establishment.mail,
                                establishmentAccount
                        ));
                    }
                });
            } else {
                throw new RuntimeException("Failed to get entity " + establishment.entity + " from establishment " + establishment.uuid);
            }
        });
    }

    @Override
    public CompletionStage<Boolean> reindex(final String organization) {
        return getAll(organization).thenCompose(establishments -> doIndexEstablishment(organization, establishments));
    }

    protected CompletionStage<Boolean> doIndexEstablishment(final String organization, final List<Establishment> establishments) {
        if (establishments.size() > 0) {
            return buildIndexableEstablishment(organization, establishments.get(0))
                    .thenCompose(indexableEstablishment -> searchService.upsert(organization, "establishments", indexableEstablishment))
                    .thenCompose(result -> {
                        establishments.remove(0);
                        return doIndexEstablishment(organization, establishments).thenApply(r -> r && result);
                    });

        } else {
            return CompletableFuture.completedFuture(true);
        }
    }

    @Override
    public CompletionStage<PaginatedResult<List<IndexableEstablishment>>> getOverviews(final String organization,
                                                                                       Pageable pageable) {

        return this.getElasticResult("establishments", organization, pageable);
    }

    @Override
    public CompletionStage<List<EstablishmentComment>> getComments(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.getComments(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<EstablishmentComment>> addComment(String organization, EstablishmentComment comment) {
        return CompletableFuture.supplyAsync(() -> this.establishmentsRepository.addComment(organization, comment));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.EstablishmentComment>> serializeComment(String organization, EstablishmentComment comment) {
        if (comment.idUser.isPresent()) {
            return usersService.get(organization, comment.idUser.get())
                    .thenApply(user -> user.map(u -> api.v1.models.EstablishmentComment.serializeComment(comment, Optional.of(u))));
        } else {
            return CompletableFuture.completedFuture(Optional.of(api.v1.models.EstablishmentComment.serializeComment(comment, Optional.empty())));
        }
    }
}
