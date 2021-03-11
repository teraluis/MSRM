package addresses;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AddressesService {

    CompletionStage<Optional<String>> add(String organization, Address address);

    CompletionStage<Optional<Address>> update(String organization, Address address);

    CompletionStage<Optional<Address>> get(String organization, String uuid);

    CompletionStage<List<Address>> getAll(String organization);

    CompletionStage<List<Address>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Address>> search(String organization, String pattern);

    CompletionStage<List<Address>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<String>> setAddress(String organization, Address address);

    CompletionStage<Optional<String>> delete(String organization, String uuid);
}
