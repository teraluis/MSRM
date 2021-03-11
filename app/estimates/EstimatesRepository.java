package estimates;

import java.util.List;
import java.util.Optional;

public interface EstimatesRepository {

    Optional<String> add(String organization, Estimate estimate);

    List<Estimate> getAll(String organization);

    List<Estimate> getPage(String organization, Integer offset, Integer length);

    List<Estimate> search(String organization, String pattern);

    List<Estimate> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<Estimate> get(String organization, String uuid);

    List<Estimate> getFromMarket(String organization, String marketUuid);

    List<Estimate> getFromAccount(String organization, String accountUuid);

    Boolean patch(String organization, String uuid, String name, Optional<String> market, Optional<String> account);

    Optional<String> delete(String organization, String uuid);

}
