package users;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleUsersService implements UsersService {

    protected final UsersRepository usersRepository;

    @Inject
    public SimpleUsersService(final UsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, User user) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.add(organization, user));
    }

    @Override
    public CompletionStage<Boolean> addGroupIfNotExists(String group) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.addGroupIfNotExists(group));
    }

    @Override
    public CompletionStage<Boolean> deleteGroupsForUser(String name) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.deleteGroupsForUser(name));
    }

    @Override
    public CompletionStage<Boolean> deleteGroup(String groupName) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.deleteGroup(groupName));
    }

    @Override
    public CompletionStage<Boolean> setGroupsForUser(String name, List<String> groups) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.setGroupsForUser(name, groups));
    }

    @Override
    public CompletionStage<List<User>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.getAll(organization));
    }

    @Override
    public CompletionStage<Optional<User>> getFromRegistrationNumber(String organization, String registration_number) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.getFromRegistrationNumber(organization, registration_number));
    }

    @Override
    public CompletionStage<List<UserWithGroups>> getUserWithGroups(String organization) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.getUserWithGroups(organization));
    }

    @Override
    public CompletionStage<List<User>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<Boolean> setActive(String user) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.setActive(user));
    }

    @Override
    public CompletionStage<Optional<User>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.delete(organization, uuid));
    }

    @Override
    public CompletionStage<List<User>> searchByFirstNameAndLastName(String organization, String value) {
        return CompletableFuture.supplyAsync(() -> this.usersRepository.searchByFirstNameAndLastName(organization, value));
    }
}
