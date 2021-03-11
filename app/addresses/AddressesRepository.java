package addresses;

import java.util.List;
import java.util.Optional;

public interface AddressesRepository {

    Optional<String> add(String organization, Address address);

    Optional<Address> update(String organization, Address address);

    Optional<Address> get(String organization, String uuid);

    List<Address> getAll(String organization);

    List<Address> getPage(String organization, Integer offset, Integer length);

    List<Address> search(String organization, String pattern);

    List<Address> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<String> delete(String organization, String uuid);
}
