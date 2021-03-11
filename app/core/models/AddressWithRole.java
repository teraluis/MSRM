package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddressWithRole {

    public final Address address;
    public final String role;

    public AddressWithRole(
            @JsonProperty("address") Address address,
            @JsonProperty("role") String role) {
        this.address = address;
        this.role = role;
    }
}
