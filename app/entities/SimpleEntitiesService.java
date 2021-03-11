package entities;

import addresses.Address;
import addresses.AddressesService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleEntitiesService implements EntitiesService {

    protected final EntitiesRepository entitiesRepository;
    protected final AddressesService addressesService;

    @Inject
    public SimpleEntitiesService(EntitiesRepository entitiesRepository, AddressesService addressesService) {
        this.entitiesRepository = entitiesRepository;
        this.addressesService = addressesService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Entity entity) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.add(organization, entity));
    }

    @Override
    public CompletionStage<Optional<Entity>> update(String organization, Entity entity) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.update(organization, entity));
    }

    @Override
    public CompletionStage<Optional<Entity>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<Entity>> getFromSiren(String organization, String siren) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.getFromSiren(organization, siren));
    }

    @Override
    public CompletionStage<List<Entity>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Entity>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Entity>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Entity>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<api.v1.models.Entity> serialize(String organization, Entity entity) {
        if (entity.mainAddress.isPresent()) {
            return addressesService.get(organization, entity.mainAddress.get())
                    .thenApply(address -> api.v1.models.Entity.serialize(entity, address.map(Address::serialize)));
        } else {
            return CompletableFuture.completedFuture(api.v1.models.Entity.serialize(entity, Optional.empty()));
        }
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.delete(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<AdnParameters>> getAdnParameters(String organization, String adnName, Optional<String> address1, Optional<String> address2, Optional<String> zip, Optional<String> city) {
        return CompletableFuture.supplyAsync(() -> this.entitiesRepository.getAdnParameters(organization, adnName, address1, address2, zip, city));
    }
}
