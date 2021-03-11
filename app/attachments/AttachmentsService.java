package attachments;

import models.AttachmentDao;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AttachmentsService {

    protected final AttachmentsRepository attachmentsRepository;

    @Inject
    public AttachmentsService(AttachmentsRepository attachmentsRepository) {
        this.attachmentsRepository = attachmentsRepository;
    }

    public CompletionStage<AttachmentDao> add(AttachmentDao attachment, String uuidOldFile) {
        return this.getOne(uuidOldFile).thenCompose(oldAttachment ->
                this.attachmentsRepository.save(attachment).thenCompose(newAttachment -> {
                    if ( oldAttachment != null ) {
                        oldAttachment.setAttachedUuid(newAttachment.getUuid());
                        return this.update(oldAttachment).thenCompose(oldUpdateAttachment ->
                                completedFuture(newAttachment)
                        );
                    } else {
                        return completedFuture(newAttachment);
                    }
                })
        );
    }

    public CompletionStage<List<AttachmentDao>> getAll(String organization) {
        return this.attachmentsRepository.getAll(organization);
    }

    public CompletionStage<AttachmentDao> getOne(String uuid) {
        return this.attachmentsRepository.findById(uuid);
    }

    public CompletionStage<List<AttachmentDao>> getAttachmentsHistory(String uuid, List<AttachmentDao> history) {
        return this.attachmentsRepository.findPrevious(uuid).thenCompose(attachmentDao -> {
            if ( attachmentDao == null ) {
                return completedFuture(history);
            } else {
                history.add(attachmentDao);
                return getAttachmentsHistory(attachmentDao.getUuid(), history);
            }
        });
    }

    public CompletionStage<List<AttachmentDao>> getLastAttachmentsByEntityId(String uuid) {
        return this.attachmentsRepository.findLastByEntityId(uuid);
    }

    public CompletionStage<AttachmentDao> save(AttachmentDao attachment) {
        return this.attachmentsRepository.save(attachment);
    }

    public CompletionStage<AttachmentDao> update(AttachmentDao attachment) {
        return this.attachmentsRepository.update(attachment);
    }
}
