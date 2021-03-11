package groups;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleGroupsService implements GroupsService {

    protected final GroupsRepository groupsRepository;

    @Inject
    public SimpleGroupsService(GroupsRepository groupsRepository) {
        this.groupsRepository = groupsRepository;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Group groupToAdd) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.add(organization, groupToAdd));
    }

    @Override
    public CompletionStage<Optional<Group>> update(String organization, Group group) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.update(organization, group));
    }

    @Override
    public CompletionStage<Optional<Group>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<Group>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Group>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Group>> getFromAccount(String organization, String accountId) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.getFromAccount(organization, accountId));
    }

    @Override
    public CompletionStage<List<Group>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Group>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.groupsRepository.delete(organization, uuid));
    }
}
