package agencies;

import java.util.List;
import java.util.Optional;

public interface AgenciesRepository {

    Optional<String> add(String organization, Agency activity);

    Optional<Agency> get(String organization, String uuid);

    List<Agency> getAll(String organization);

    List<Agency> getPage(String organization, Integer offset, Integer length);

    Optional<Agency> getFromOfficeName(String organization, String officeName);

    List<Agency> search(String organization, String pattern);

    List<Agency> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<Agency> update(String organization, Agency activity);

    Optional<String> delete(String organization, String uuid);
}
