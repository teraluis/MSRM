package estimates;

import core.Single;

import java.util.Optional;
import java.util.UUID;

public class Estimate implements Single<String> {

    public final String uuid;
    public final String name;
    public final Optional<String> market;
    public final Optional<String> account;

    public Estimate(final Optional<String> uuid, final String name, final Optional<String> market, final Optional<String> account) {
        this.uuid = uuid.orElseGet(() -> "estimate-" + UUID.randomUUID());
        this.name = name;
        this.market = market;
        this.account = account;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
