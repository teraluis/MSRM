package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class People {

    public final String uuid;
    public final String title;
    public final String lastname;
    public final String firstname;
    public final Optional<String> workMail;
    public final Optional<String> workPhone;
    public final Optional<String> email;
    public final Optional<String> mobilePhone;
    public final Optional<String> jobDescription;
    public final List<AddressWithRole> addresses;

    public People(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("title") String title,
            @JsonProperty("lastname") String lastname,
            @JsonProperty("firstname") String firstname,
            @JsonProperty("workMail") String workMail,
            @JsonProperty("workPhone") String workPhone,
            @JsonProperty("email") String email,
            @JsonProperty("mobilePhone") String mobilePhone,
            @JsonProperty("jobDescription") String jobDescription,
            @JsonProperty("addresses") List<AddressWithRole> addresses) {
        this.uuid = uuid;
        this.title = title;
        this.lastname = lastname;
        this.firstname = firstname;
        this.workMail = Optional.ofNullable(workMail);
        this.workPhone = Optional.ofNullable(workPhone);
        this.email = Optional.ofNullable(email);
        this.mobilePhone = Optional.ofNullable(mobilePhone);
        this.jobDescription = Optional.ofNullable(jobDescription);
        this.addresses = addresses;
    }
}
