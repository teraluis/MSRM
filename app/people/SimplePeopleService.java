package people;

import addresses.Address;
import addresses.AddressWithRole;
import addresses.AddressesService;
import core.CompletableFutureUtils;
import core.models.PeopleWithOrigin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class SimplePeopleService implements PeopleService {

    protected final PeopleRepository peopleRepository;
    protected final AddressesService addressesService;
    protected static final Logger logger = LoggerFactory.getLogger(SimplePeopleService.class);

    @Inject
    public SimplePeopleService(PeopleRepository peopleRepository, AddressesService addressesService) {
        this.peopleRepository = peopleRepository;
        this.addressesService = addressesService;
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.delete(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, People people) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.add(organization, people));
    }

    @Override
    public CompletionStage<Optional<People>> update(String organization, People people) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.update(organization, people));
    }

    @Override
    public CompletionStage<List<People>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<People>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<Boolean> setAdresses(String organization, Optional<Address> mainAddress, Optional<Address> billingAddress, Optional<Address> deliveryAddress) {
        return mainAddress.map(address -> addressesService.setAddress(organization, address).thenApply(Optional::isPresent)).orElse(CompletableFuture.completedFuture(true))
                .thenCompose(mainAddressDone -> {
                    if (mainAddressDone) {
                        return billingAddress.map(address -> addressesService.setAddress(organization, address).thenApply(Optional::isPresent)).orElse(CompletableFuture.completedFuture(true))
                                .thenCompose(billingAddressDone -> {
                                    if (billingAddressDone) {
                                        return deliveryAddress.map(address -> addressesService.setAddress(organization, address).thenApply(Optional::isPresent)).orElse(CompletableFuture.completedFuture(true));
                                    } else {
                                        logger.error("failed to add billing address");
                                        return CompletableFuture.completedFuture(false);
                                    }
                                });
                    } else {
                        logger.error("failed to insert main address");
                        return CompletableFuture.completedFuture(false);
                    }
                });
    }

    @Override
    public CompletionStage<List<People>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> {
            List<People> people = this.peopleRepository.search(organization, pattern);
            SearchPeopleComparator comparator = new SearchPeopleComparator(pattern);
            people.sort(comparator);
            return people;
        });
    }

    @Override
    public CompletionStage<List<People>> searchPage(String organization, String pattern, Integer
            offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> {
            List<People> people = this.peopleRepository.searchPage(organization, pattern, offset, length);
            SearchPeopleComparator comparator = new SearchPeopleComparator(pattern);
            people.sort(comparator);
            return people;
        });
    }

    @Override
    public CompletionStage<Optional<String>> setPeople(String organization, People people) {
        return get(organization, people.uuid).thenCompose(optionalPeople -> {
            if (optionalPeople.isPresent()) {
                return update(organization, people).thenApply(finalPeople -> finalPeople.map(People::getId));
            } else {
                return add(organization, people);
            }
        });

    }

    @Override
    public CompletionStage<core.models.People> serialize(String organization, People people) {
        return getAddresses(organization, people.uuid)
                .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address).thenApply(address -> new core.models.AddressWithRole(address.get().serialize(), addressWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                .thenApply(finalAddresses -> people.serialize(finalAddresses));
    }

    @Override
    public CompletionStage<Optional<People>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<AddressWithRole>> getAddressesByRole(String organization, String uuid, String role) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.getAddressesByRole(organization, uuid, role));
    }

    @Override
    public CompletionStage<List<AddressWithRole>> getAddresses(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.getAddresses(organization, uuid));
    }

    @Override
    public CompletionStage<Boolean> addAddress(String peopleId, String addressId, String role) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.addAddress(peopleId, addressId, role));
    }

    @Override
    public CompletionStage<Boolean> removeAddress(String peopleId, String addressId, String role) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.removeAddress(peopleId, addressId, role));
    }

    @Override
    public CompletionStage<List<PeopleWithOrigin>> getPurchasers(String organization, String establishment, Optional<String> market) {
        return CompletableFuture.supplyAsync(() -> this.peopleRepository.getPurchasers(organization, establishment, market))
                .thenCompose(tuples -> CompletableFutureUtils.sequence(tuples.stream().map(tuple -> serialize(organization, tuple._1).thenApply(finalPeople -> new PeopleWithOrigin(finalPeople, tuple._2)).toCompletableFuture()).collect(Collectors.toList())));
    }
}
