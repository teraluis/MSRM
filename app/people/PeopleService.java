package people;

import addresses.Address;
import addresses.AddressWithRole;
import core.models.PeopleWithOrigin;
import scala.Tuple2;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface PeopleService {

    CompletionStage<Optional<String>> add(String organization, People people);

    CompletionStage<Optional<People>> update(String organization, People people);

    CompletionStage<List<People>> getAll(String organization);

    CompletionStage<List<People>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<Boolean> setAdresses(String organization, Optional<Address> mainAddress, Optional<Address> billingAddress, Optional<Address> deliveryAddress);

    CompletionStage<List<People>> search(String organization, String pattern);

    CompletionStage<List<People>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<String>> setPeople(String organization, People people);

    CompletionStage<core.models.People> serialize(String organization, People people);

    CompletionStage<Optional<People>> get(String organization, String uuid);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<List<AddressWithRole>> getAddressesByRole(String organization, String peopleId, String role);

    CompletionStage<List<AddressWithRole>> getAddresses(String organization, String peopleId);

    CompletionStage<Boolean> addAddress(String peopleId, String addressId, String role);

    CompletionStage<Boolean> removeAddress(String peopleId, String addressId, String role);

    CompletionStage<List<PeopleWithOrigin>> getPurchasers(String organization, String establishment, Optional<String> market);
}
