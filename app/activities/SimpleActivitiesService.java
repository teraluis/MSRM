package activities;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleActivitiesService implements ActivitiesService {

    protected final ActivitiesRepository activitiesRepository;

    @Inject
    public SimpleActivitiesService(ActivitiesRepository activitiesRepository) {
        this.activitiesRepository = activitiesRepository;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Activity activityToAdd) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.add(organization, activityToAdd));
    }

    @Override
    public CompletionStage<Optional<Activity>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<Activity>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Activity>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Activity>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Activity>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Optional<Activity>> update(String organization, Activity activity) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.update(organization, activity));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.activitiesRepository.delete(organization, uuid));
    }
}
