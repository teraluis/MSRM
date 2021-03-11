package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeopleWithRole {

    public final People people;
    public final String role;

    public PeopleWithRole(
            @JsonProperty("people") People people,
            @JsonProperty("role") String role) {
        this.people = people;
        this.role = role;
    }
}
