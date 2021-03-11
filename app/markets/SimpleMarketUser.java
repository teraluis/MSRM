package markets;

import java.util.Objects;

public class SimpleMarketUser {
    public String user;
    public String role;

    public SimpleMarketUser(
            String user,
            String role
    ) {
        this.user = user;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMarketUser)) return false;
        SimpleMarketUser that = (SimpleMarketUser) o;
        return user.equals(that.user);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user);
    }
}
