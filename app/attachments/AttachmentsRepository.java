package attachments;

import io.ebean.Ebean;
import io.ebean.EbeanServer;
import io.ebean.Transaction;
import models.AttachmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.db.ebean.EbeanConfig;

import javax.inject.Inject;
import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class AttachmentsRepository {

    public final static String TENANT = "tenant";
    public final static String VFK = "vfk";
    public final static String CREATED = "created";
    public final static String ATTACHED_UUID = "attached_uuid";

    private final Logger log = LoggerFactory.getLogger(AttachmentsRepository.class);
    private final EbeanServer ebeanServer;

    @Inject
    public AttachmentsRepository(EbeanConfig ebeanConfig) {
        this.ebeanServer = Ebean.getServer(ebeanConfig.defaultServer());
    }

    public CompletableFuture<AttachmentDao> findById(String uuid) {
        return supplyAsync(() -> ebeanServer.find(AttachmentDao.class).where().idEq(uuid).findOne());
    }

    public CompletableFuture<List<AttachmentDao>> findByEntityId(String uuid) {
        return supplyAsync(() -> ebeanServer.find(AttachmentDao.class).where().eq(VFK, uuid).findList());
    }

    public CompletableFuture<AttachmentDao> findPrevious(String uuid) {
        return supplyAsync(() -> ebeanServer.find(AttachmentDao.class).where().eq(ATTACHED_UUID, uuid).findOne());
    }

    public CompletableFuture<List<AttachmentDao>> findLastByEntityId(String uuid) {
        return supplyAsync(() -> ebeanServer.find(AttachmentDao.class).where().eq(VFK, uuid).eq(ATTACHED_UUID, null).findList());
    }

    public CompletionStage<List<AttachmentDao>> getAll(String organization) {
        return supplyAsync(() -> ebeanServer.find(AttachmentDao.class).where().eq(TENANT, organization).findList());
    }

    public CompletionStage<AttachmentDao> save(AttachmentDao attachment) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                attachment.save();
                txn.commit();
            } catch (OptimisticLockException e) {
                log.error("Error when inserting new attachment", e);
                txn.rollback();
            } finally {
                txn.end();
            }
            return attachment;
        });
    }

    public CompletionStage<AttachmentDao> update(AttachmentDao attachment) {
        return supplyAsync(() -> {
            Transaction txn = ebeanServer.beginTransaction();
            try {
                attachment.update();
                txn.commit();
            } catch (OptimisticLockException e) {
                log.error("Error when inserting new attachment", e);
                txn.rollback();
            } finally {
                txn.end();
            }
            return attachment;
        });
    }
}
