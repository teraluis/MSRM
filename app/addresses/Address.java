package addresses;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Address implements Single<String> {

    public final String uuid;
    public final String type;
    public final Optional<String> address1;
    public final Optional<String> address2;
    public final Optional<String> postCode;
    public final Optional<String> city;
    public final Optional<String> gpsCoordinates;
    public final Optional<String> inseeCoordinates;
    public final Optional<String> dispatch;
    public final Optional<String> staircase;
    public final Optional<String> wayType;
    public final Optional<String> country;
    public final Date created;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Address(
            @JsonProperty("uuid") final Optional<String> uuid,
            @JsonProperty("type") final String type,
            @JsonProperty("address1") final Optional<String> address1,
            @JsonProperty("address2") final Optional<String> address2,
            @JsonProperty("postCode") final Optional<String> postCode,
            @JsonProperty("city") final Optional<String> city,
            @JsonProperty("gpsCoordinates") final Optional<String> gpsCoordinates,
            @JsonProperty("inseeCoordinates") final Optional<String> inseeCoordinates,
            @JsonProperty("dispatch") final Optional<String> dispatch,
            @JsonProperty("staircase") final Optional<String> staircase,
            @JsonProperty("wayType") final Optional<String> wayType,
            @JsonProperty("country") final Optional<String> country,
            @JsonProperty("created") final Date created
    ) {
        this.uuid = uuid.orElseGet(() -> "address-" + UUID.randomUUID());
        this.type = type;
        this.address1 = address1;
        this.address2 = address2;
        this.postCode = postCode;
        this.city = city;
        this.gpsCoordinates = gpsCoordinates;
        this.inseeCoordinates = inseeCoordinates;
        this.dispatch = dispatch;
        this.staircase = staircase;
        this.wayType = wayType;
        this.country = country;
        this.created = created;
    }

    @Override
    public String getId() {
        return uuid;
    }

    public core.models.Address serialize() {
        return new core.models.Address(uuid, type, address1.orElse(null), address2.orElse(null), postCode.orElse(null), city.orElse(null), gpsCoordinates.orElse(null),
                inseeCoordinates.orElse(null), dispatch.orElse(null), staircase.orElse(null), wayType.orElse(null), country.orElse(null), created.getTime());
    }

}
