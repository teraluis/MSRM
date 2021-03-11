package users;

import java.util.List;

public class Group {
    public final String name;
    public final List<User> user;

    public Group(final String name, final List<User> user) {
        this.name = name;
        this.user = user;
    }
}
