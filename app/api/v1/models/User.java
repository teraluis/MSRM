package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class User {

    public final String login;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> registration_number;
    public final String first_name;
    public final String last_name;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> office;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> phone;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User(
            @JsonProperty("login") final String login,
            @JsonProperty("registration_number") final String registration_number,
            @JsonProperty("first_name") final String first_name,
            @JsonProperty("last_name") final String last_name,
            @JsonProperty("office") final String office,
            @JsonProperty("phone") final String phone,
            @JsonProperty("description") final String description) {
        this.login = login;
        this.registration_number = Optional.ofNullable(registration_number);
        this.first_name = first_name;
        this.last_name = last_name;
        this.office = Optional.ofNullable(office);
        this.phone = Optional.ofNullable(phone);
        this.description = Optional.ofNullable(description);
    }

    public static User serialize(users.User user) {
        return new User(
                user.login,
                user.registration_number.orElse(null),
                user.first_name,
                user.last_name,
                user.office.orElse(null),
                user.phone.orElse(null),
                user.description.orElse(null)
        );
    }
}
