package missionclient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class InterventionEstate {

    public final Optional<String> address1;
    public final Optional<String> address2;
    public final Optional<String> zip;
    public final Optional<String> city;

    public InterventionEstate(
            @JsonProperty("address1") String address1,
            @JsonProperty("address2") String address2,
            @JsonProperty("zip") String zip,
            @JsonProperty("city") String city) {
        this.address1 = Optional.ofNullable(address1);
        this.address2 = Optional.ofNullable(address2);
        this.zip = Optional.ofNullable(zip);
        this.city = Optional.ofNullable(city);
    }
}
