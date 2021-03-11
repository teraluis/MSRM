package agencies;

import office.OfficeService;
import users.UsersService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleAgenciesService implements AgenciesService {

    protected final AgenciesRepository agenciesRepository;
    protected final UsersService usersService;
    protected final OfficeService officeService;

    @Inject
    public SimpleAgenciesService(AgenciesRepository agenciesRepository, UsersService usersService, OfficeService officeService) {
        this.agenciesRepository = agenciesRepository;
        this.usersService = usersService;
        this.officeService = officeService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Agency activityToAdd) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.add(organization, activityToAdd));
    }

    @Override
    public CompletionStage<Optional<Agency>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<List<Agency>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Agency>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<Optional<Agency>> getFromOfficeName(String organization, String officeName) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.getFromOfficeName(organization, officeName));
    }

    @Override
    public CompletionStage<List<Agency>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Agency>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<api.v1.models.Agency> serialize(String organization, Agency agency) {
        return usersService.get(organization, agency.manager).thenCompose(manager ->
                officeService.getByAgencyId(agency.uuid).thenCompose(officies ->
                        CompletableFuture.completedFuture(api.v1.models.Agency.serialize(agency, manager.get(), officies))
                )
        );
    }

    @Override
    public CompletionStage<Optional<Agency>> update(String organization, Agency agency) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.update(organization, agency));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.agenciesRepository.delete(organization, uuid));
    }
}
