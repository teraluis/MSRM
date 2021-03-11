package office;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.OfficeDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class OfficeRepository {

    private final Logger log = LoggerFactory.getLogger(OfficeRepository.class);
    private final EbeanServer ebeanServer;

    @Inject
    public OfficeRepository(EbeanConfig ebeanConfig) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    public CompletableFuture<OfficeDao> findById(String uuid) {
        return supplyAsync(() -> ebeanServer.find(OfficeDao.class).where().idEq(uuid).findOne());
    }

    public CompletableFuture<OfficeDao> findByName(String name) {
        return supplyAsync(() -> ebeanServer.find(OfficeDao.class).where().eq("name", name).findOne());
    }

    public CompletableFuture<List<OfficeDao>> findByAgencyId(String agencyId) {
        return supplyAsync(() -> ebeanServer.find(OfficeDao.class).where().eq("id_agency", agencyId).findList());
    }

    public CompletionStage<List<OfficeDao>> getAll() {
        return supplyAsync(() -> ebeanServer.find(OfficeDao.class).findList());
    }

    public CompletionStage<List<OfficeDao>> searchByName(String value) {
        return supplyAsync(() -> ebeanServer.find(OfficeDao.class).where().icontains("name", value).findList());
    }

    public CompletionStage<OfficeDao> save(OfficeDao office) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                office.save();
                txn.commit();
            } catch (OptimisticLockException e) {
                log.error("Error when inserting new attachment", e);
                txn.rollback();
            } finally {
                txn.end();
            }
            return office;
        });
    }

    public CompletionStage<OfficeDao> update(OfficeDao office) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                office.update();
                txn.commit();
            } catch (OptimisticLockException e) {
                log.error("Error when inserting new attachment", e);
                txn.rollback();
            } finally {
                txn.end();
            }
            return office;
        });
    }
}
