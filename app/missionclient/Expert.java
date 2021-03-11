package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Expert {
    public final String uuid;
    public final String firstname;
    public final String lastname;
    public final Optional<String> pager;

    @JsonCreator
    public Expert(
            @JsonProperty("id") String id,
            @JsonProperty("firstName") String firstname,
            @JsonProperty("lastName") String lastname,
            @JsonProperty("pager") String pager) {
        this.uuid = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.pager = Optional.ofNullable(pager);
    }

}
