package addresses;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleAddressesService implements AddressesService {

    protected final AddressesRepository addressesRepository;

    @Inject
    public SimpleAddressesService(final AddressesRepository addressesRepository) {
        this.addressesRepository = addressesRepository;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Address addressToAdd) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.add(organization, addressToAdd));
    }

    @Override
    public CompletionStage<Optional<Address>> update(String organization, Address address) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.update(organization, address));
    }

    @Override
    public CompletionStage<Optional<Address>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<Address>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Address>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Address>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Address>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Optional<String>> setAddress(String organization, Address address) {
        return get(organization, address.uuid).thenCompose(optionalAddress -> {
            if (optionalAddress.isPresent()) {
                return update(organization, address).thenApply(finalAddress -> finalAddress.map(Address::getId));
            } else {
                return add(organization, address);
            }
        });
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.addressesRepository.delete(organization, uuid));
    }

}
