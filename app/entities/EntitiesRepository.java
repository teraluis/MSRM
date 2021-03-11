package entities;

import java.util.List;
import java.util.Optional;

public interface EntitiesRepository {

    Optional<String> add(String organization, Entity entity);

    Optional<Entity> update(String organization, Entity entity);

    Optional<Entity> get(String organization, String uuid);

    Optional<Entity> getFromSiren(String organization, String siren);

    List<Entity> getAll(String organization);

    List<Entity> getPage(String organization, Integer offset, Integer length);

    List<Entity> search(String organization, String pattern);

    List<Entity> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<String> delete(String organization, String uuid);

    Optional<AdnParameters> getAdnParameters(String organization, String adnName, Optional<String> address1, Optional<String> address2, Optional<String> zip, Optional<String> city);
}
