package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Address {
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
    public final Long created;

    public Address(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("type") String type,
            @JsonProperty("address1") String address1,
            @JsonProperty("address2") String address2,
            @JsonProperty("postCode") String postCode,
            @JsonProperty("city") String city,
            @JsonProperty("gpsCoordinates") String gpsCoordinates,
            @JsonProperty("inseeCoordinates") String inseeCoordinates,
            @JsonProperty("dispatch") String dispatch,
            @JsonProperty("staircase") String staircase,
            @JsonProperty("wayType") String wayType,
            @JsonProperty("country") String country,
            @JsonProperty("created") Long created) {
        this.uuid = uuid;
        this.type = type;
        this.address1 = Optional.ofNullable(address1);
        this.address2 = Optional.ofNullable(address2);
        this.postCode = Optional.ofNullable(postCode);
        this.city = Optional.ofNullable(city);
        this.gpsCoordinates = Optional.ofNullable(gpsCoordinates);
        this.inseeCoordinates = Optional.ofNullable(inseeCoordinates);
        this.dispatch = Optional.ofNullable(dispatch);
        this.staircase = Optional.ofNullable(staircase);
        this.wayType = Optional.ofNullable(wayType);
        this.country = Optional.ofNullable(country);
        this.created = created;
    }

}
