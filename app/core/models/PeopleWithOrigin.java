package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PeopleWithOrigin {

    public final People people;
    public final String origin;

    public PeopleWithOrigin(
            @JsonProperty("people") People people,
            @JsonProperty("origin") String origin) {
        this.people = people;
        this.origin = origin;
    }

}
