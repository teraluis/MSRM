package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.People;
import users.User;

import java.util.Objects;
import java.util.Optional;

public class Account {

    public final String uuid;
    public final String type;
    public final String reference;
    public final String category;
    public final User commercial;
    public final People contact;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> importance;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> state;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Entity> entity;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Integer> maxPaymentTime;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> legacyCode;
    public final Long created;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Account(
            @JsonProperty("uuid")
            final String uuid,
            @JsonProperty("type")
            final String type,
            @JsonProperty("reference")
            final String reference,
            @JsonProperty("category")
            final String category,
            @JsonProperty("commercial")
            final User commercial,
            @JsonProperty("contact")
            final People contact,
            @JsonProperty("importance")
            final Optional<String> importance,
            @JsonProperty("state")
            final Optional<String> state,
            @JsonProperty("entity")
            final Optional<Entity> entity,
            @JsonProperty("maxPaymentTime")
            final Optional<Integer> maxPaymentTime,
            @JsonProperty("legacyCode")
            final Optional<String> legacyCode,
            @JsonProperty("created")
            final Long created) {
        this.uuid = uuid;
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

    public static Account serialize(accounts.Account account, User commercial, People contact, Optional<Entity> entity) {
        return new Account(account.uuid, account.type, account.reference, account.category, commercial, contact,
                account.importance, account.state, entity, account.maxPaymentTime, account.legacyCode, account.created.getTime());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Account)) return false;
        Account that = (Account) o;
        return uuid.equals(that.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid);
    }

}
