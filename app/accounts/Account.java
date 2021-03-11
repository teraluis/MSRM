package accounts;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Account implements Single<String> {

    public final String uuid;
    public final String type;
    public final String reference;
    public final String category;
    public final String commercial;
    public final String contact;
    public final Optional<String> importance;
    public final Optional<String> state;
    public final Optional<String> entity;
    public final Optional<Integer> maxPaymentTime;
    public final Optional<String> legacyCode;
    public final Date created;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Account(
            final Optional<String> uuid,
            @JsonProperty("type")
            final String type,
            @JsonProperty("reference")
            final String reference,
            @JsonProperty("category")
            final String category,
            @JsonProperty("commercial")
            final String commercial,
            @JsonProperty("contact")
            final String contact,
            @JsonProperty("importance")
            final Optional<String> importance,
            @JsonProperty("state")
            final Optional<String> state,
            @JsonProperty("entity")
            final Optional<String> entity,
            @JsonProperty("maxPaymentTime")
            final Optional<Integer> maxPaymentTime,
            @JsonProperty("legacyCode")
            final Optional<String> legacyCode,
            @JsonProperty("created")
            final Date created
    ) {
        this.uuid = uuid.orElseGet(() -> "account-" + UUID.randomUUID());
        this.type = type;
        this.reference = reference;
        this.category = category;
        this.commercial = commercial;
        this.contact = contact;
        this.importance = importance;
        this.state = state;
        this.entity = entity;
        this.maxPaymentTime = maxPaymentTime;
        this.legacyCode = legacyCode;
        this.created = created;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
