package groups;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface GroupsService {

    CompletionStage<Optional<String>> add(String organization, Group group);

    CompletionStage<Optional<Group>> update(String organization, Group group);

    CompletionStage<Optional<Group>> get(String organization, String uuid);

    CompletionStage<List<Group>> getAll(String organization);

    CompletionStage<List<Group>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Group>> getFromAccount(String organization, String accountId);

    CompletionStage<List<Group>> search(String organization, String pattern);

    CompletionStage<List<Group>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<String>> delete(String organization, String uuid);
}
