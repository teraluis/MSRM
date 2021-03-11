package users;

import java.util.List;
import java.util.Optional;

public interface UsersRepository {

    Optional<String> add(String organization, User user);

    Boolean addGroupIfNotExists(String group);

    Boolean deleteGroupsForUser(String name);

    Boolean deleteGroup(String groupName);

    Boolean setGroupsForUser(String name, List<String> groups);

    List<User> getAll(String organization);

    Optional<User> getFromRegistrationNumber(String organization, String registration_number);

    List<UserWithGroups> getUserWithGroups(String organization);

    List<User> search(String organization, String pattern);

    Boolean setActive(String user);

    Optional<User> get(String organization, String uuid);

    Optional<String> delete(String organization, String uuid);

    List<User> searchByFirstNameAndLastName(String organization, String value);

}
