package entities;

import addresses.Address;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface EntitiesService {

    CompletionStage<Optional<String>> add(String organization, Entity entity);

    CompletionStage<Optional<Entity>> update(String organization, Entity entity);

    CompletionStage<Optional<Entity>> get(String organization, String uuid);

    CompletionStage<Optional<Entity>> getFromSiren(String organization, String siren);

    CompletionStage<List<Entity>> getAll(String organization);

    CompletionStage<List<Entity>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Entity>> search(String organization, String pattern);

    CompletionStage<List<Entity>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<api.v1.models.Entity> serialize(String organization, Entity entity);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<Optional<AdnParameters>> getAdnParameters(String organization, String adnName, Optional<String> address1, Optional<String> address2, Optional<String> zip, Optional<String> city);
}
