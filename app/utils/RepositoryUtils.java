package utils;

import core.Single;

import java.util.Optional;
import java.util.function.BiFunction;

public class RepositoryUtils {

    public static  <T1 extends Single<String>> Optional<String> getAndUpdateExistingItem(final String organization, final Optional<T1> item, final BiFunction<String, T1, T1> addFunction) {
        // Get main address
        Optional<String> itemId = item.map(i -> i.getId());

        // Add main address if not present
        itemId.ifPresent(ma -> {
            addFunction.apply(organization, item.get());
        });

        return itemId;
    }
}
