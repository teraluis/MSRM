package users;

import java.util.List;

public class UserWithGroups {

    public final String username;
    public final List<String> groups;

    public UserWithGroups(String username, List<String> groups) {
        this.username = username;
        this.groups = groups;
    }
}
