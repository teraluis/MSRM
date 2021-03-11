package users;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface UsersService {

    CompletionStage<Optional<String>> add(String organization, User user);

    CompletionStage<Boolean> addGroupIfNotExists(String group);

    CompletionStage<Boolean> deleteGroupsForUser(String name);

    CompletionStage<Boolean> deleteGroup(String groupName);

    CompletionStage<Boolean> setGroupsForUser(String name, List<String> groups);

    CompletionStage<List<User>> getAll(String organization);

    CompletionStage<Optional<User>> getFromRegistrationNumber(String organization, String registration_number);

    CompletionStage<List<UserWithGroups>> getUserWithGroups(String organization);

    CompletionStage<List<User>> search(String organization, String pattern);

    CompletionStage<Boolean> setActive(String user);

    CompletionStage<Optional<User>> get(String organization,String uuid);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<List<User>> searchByFirstNameAndLastName(String organization, String value);

}
