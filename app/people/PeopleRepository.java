package people;

import addresses.AddressWithRole;
import core.models.PeopleWithOrigin;
import scala.Tuple2;

import java.util.List;
import java.util.Optional;

public interface PeopleRepository {

    Optional<String> add(String organization, People people);

    Optional<People> update(String organization, People people);

    List<People> getAll(String organization);

    List<People> getPage(String organization, Integer offset, Integer length);

    List<People> search(String organization, String pattern);

    List<People> searchPage(String organization, String pattern, Integer offset, Integer length);

    List<Tuple2<People, String>> getPurchasers(String organization, String establishment, Optional<String> market);

    Optional<People> get(String organization, String uuid);

    Optional<String> delete(String organization, String uuid);

    List<AddressWithRole> getAddressesByRole(String organization, String establishmentId, String role);

    List<AddressWithRole> getAddresses(String organization, String establishmentId);

    Boolean addAddress(String establishmentId, String addressId, String role);

    Boolean removeAddress(String establishmentId, String addressId, String role);
}
