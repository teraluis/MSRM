package office;

import models.OfficeDao;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class OfficeService {

    protected final OfficeRepository officeRepository;

    @Inject
    public OfficeService(OfficeRepository officeRepository) {
        this.officeRepository = officeRepository;
    }

    public CompletionStage<OfficeDao> add(OfficeDao office) {
        return this.officeRepository.save(office).thenCompose(CompletableFuture::completedFuture);
    }

    public CompletionStage<List<OfficeDao>> getAll() {
        return this.officeRepository.getAll();
    }

    public CompletionStage<OfficeDao> getOne(String uuid) {
        return this.officeRepository.findById(uuid);
    }

    public CompletionStage<OfficeDao> getOneByName(String name) {
        return this.officeRepository.findByName(name);
    }

    public CompletionStage<List<OfficeDao>> getByAgencyId(String agencyId) {
        return this.officeRepository.findByAgencyId(agencyId);
    }

    public CompletionStage<List<OfficeDao>> searchByName(String value) {
        return this.officeRepository.searchByName(value);
    }

    public CompletionStage<OfficeDao> save(OfficeDao office) {
        return this.officeRepository.save(office);
    }

    public CompletionStage<OfficeDao> update(OfficeDao office) {
        return this.officeRepository.update(office);
    }
}
