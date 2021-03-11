package groups;

import java.util.List;
import java.util.Optional;

public interface GroupsRepository {

    Optional<String> add(String organization, Group group);

    Optional<Group> update(String organization, Group group);

    Optional<Group> get(String organization, String uuid);

    List<Group> getAll(String organization);

    List<Group> getPage(String organization, Integer offset, Integer length);

    List<Group> getFromAccount(String organization, String accountId);

    List<Group> search(String organization, String pattern);

    List<Group> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<String> delete(String organization, String uuid);
}
