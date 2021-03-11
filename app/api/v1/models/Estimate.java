package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Optional;

public class Estimate {

    public final String uuid;
    public final String name;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<FullMarket> market;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Account> account;

    public Estimate(final String uuid, final String name, final Optional<FullMarket> market, final Optional<Account> account) {
        this.uuid = uuid;
        this.name = name;
        this.market = market;
        this.account = account;
    }

    public static Estimate serialize(estimates.Estimate estimate, Optional<FullMarket> market, Optional<Account> account) {
        return new Estimate(estimate.uuid, estimate.name, market, account);
    }

}
