package markets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import users.User;

import java.util.Objects;

public class MarketUser {
    public User user;
    public String role;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MarketUser(
            @JsonProperty("user")
            User user,
            @JsonProperty("role")
            String role
    ) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketUser)) return false;
        MarketUser that = (MarketUser) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
