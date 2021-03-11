package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.Address;

import java.util.Optional;

public class Entity {

    public final String uuid;
    public final String name;
    public final String corporateName;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> type;
    public final String siren;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> domain;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> logo;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Address> mainAddress;
    public final Long created;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Entity(
            @JsonProperty("uuid") final String uuid,
            @JsonProperty("name") final String name,
            @JsonProperty("corporateName") final String corporateName,
            @JsonProperty("type") final Optional<String> type,
            @JsonProperty("siren") final String siren,
            @JsonProperty("domain") final Optional<String> domain,
            @JsonProperty("logo") final Optional<String> logo,
            @JsonProperty("description") final Optional<String> description,
            @JsonProperty("mainAddress") final Optional<Address> mainAddress,
            @JsonProperty("created") final Long created
    ) {
        this.uuid = uuid;
        this.name = name;
        this.corporateName = corporateName;
        this.type = type;
        this.siren = siren;
        this.domain = domain;
        this.logo = logo;
        this.description = description;
        this.mainAddress = mainAddress;
        this.created = created;
    }

    public static Entity serialize(entities.Entity entity, Optional<Address> address) {
        return new Entity(entity.uuid, entity.name, entity.corporateName, entity.type, entity.siren, entity.domain,
                entity.logo, entity.description, address, entity.created.getTime());
    }

}
