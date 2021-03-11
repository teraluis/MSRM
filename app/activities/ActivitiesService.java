package activities;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface ActivitiesService {

    CompletionStage<Optional<String>> add(String organization, Activity activity);

    CompletionStage<Optional<Activity>> get(String organization, String uuid);

    CompletionStage<List<Activity>> getAll(String organization);

    CompletionStage<List<Activity>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Activity>> search(String organization, String pattern);

    CompletionStage<List<Activity>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<Activity>> update(String organization, Activity activity);

    CompletionStage<Optional<String>> delete(String organization, String uuid);
}
