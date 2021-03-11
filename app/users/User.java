package users;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Single;

import java.util.Objects;
import java.util.Optional;

public class User implements Single<String> {

    public final String login;
    public final Optional<String> registration_number;
    public final String first_name;
    public final String last_name;
    public final Optional<String> office;
    public final Optional<String> phone;
    public final Optional<String> description;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public User(
            @JsonProperty("login")
            final String login,
            @JsonProperty("registration_number")
            final Optional<String> registration_number,
            @JsonProperty("first_name")
            final String first_name,
            @JsonProperty("last_name")
            final String last_name,
            @JsonProperty("office")
            final Optional<String> office,
            @JsonProperty("phone")
            final Optional<String> phone,
            @JsonProperty("description")
            final Optional<String> description
    ) {
        this.login = login;
        this.registration_number = registration_number;
        this.first_name = first_name;
        this.last_name = last_name;
        this.office = office;
        this.phone = phone;
        this.description = description;
    }

    @Override
    public String getId() {
        return login;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User)) return false;
        User user = (User) o;
        return login.equals(user.login);
    }

    @Override
    public int hashCode() {
        return Objects.hash(login);
    }
}
