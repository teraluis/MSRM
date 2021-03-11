package activities;

import java.util.List;
import java.util.Optional;

public interface ActivitiesRepository {

    Optional<String> add(String organization, Activity activity);

    Optional<Activity> get(String organization, String uuid);

    List<Activity> getAll(String organization);

    List<Activity> getPage(String organization, Integer offset, Integer length);

    List<Activity> search(String organization, String pattern);

    List<Activity> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<Activity> update(String organization, Activity activity);

    Optional<String> delete(String organization, String uuid);
}
