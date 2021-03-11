package api.v1.models;

import users.User;

public class MarketUser {
    public final User user;
    public final String role;

    public MarketUser(User user, String role) {
        this.user = user;
        this.role = role;
    }

    public static MarketUser serialize(User user, String role) {
        return new MarketUser(user, role);
    }
}
