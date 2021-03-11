package api.v1.controllers;

import attachments.AttachmentsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import core.ErrorMessage;
import core.UUIDJson;
import io.minio.ObjectStat;
import models.AttachmentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Files;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import services.FileService;

import javax.inject.Inject;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AttachmentsController extends Controller {
    protected static final Logger logger = LoggerFactory.getLogger(AttachmentsController.class);

    private final AttachmentsService attachmentsService;
    private final FileService fileService;

    @Inject
    AttachmentsController(final AttachmentsService attachmentsService, FileService fileService) {
        this.attachmentsService = attachmentsService;
        this.fileService = fileService;
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        return attachmentsService.getAll(organization).thenApply(attachments -> ok(Json.toJson(attachments)));
    }

    public CompletionStage<Result> getAttachment(final String organization, final String uuid) {
        return attachmentsService.getOne(uuid).thenCompose(attachment -> {
            if (attachment != null) {
                return completedFuture(ok(Json.toJson(attachment)));
            } else {
                return completedFuture(badRequest(Json.toJson(new ErrorMessage("No attachment with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getAttachmentsByEntityId(final String organization, final String uuid) {
        return attachmentsService.getLastAttachmentsByEntityId(uuid).thenCompose(attachments -> {
            if (attachments != null) {
                return completedFuture(ok(Json.toJson(attachments)));
            } else {
                return completedFuture(badRequest(Json.toJson(new ErrorMessage("No attachments found with uuid " + uuid + " in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getAttachmentHistory(final String organization, final String uuid) {
        return attachmentsService.getAttachmentsHistory(uuid, new ArrayList<>()).thenCompose(attachments -> {
            if (attachments != null) {
                return completedFuture(ok(Json.toJson(attachments)));
            } else {
                return completedFuture(badRequest(Json.toJson(new ErrorMessage("No attachments found with uuid " + uuid + " in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getAttachmentFile(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("collection") && entries.containsKey("filename")) {
            String collection = entries.get("collection")[0];
            String filename = entries.get("filename")[0];
            InputStream input = this.fileService.download(organization, uuid, collection, filename);
            ObjectStat stat = this.fileService.getStatObject(organization, uuid, collection, filename);
            return CompletableFuture.completedFuture(ok(input)
                    .as(stat.contentType())
                    .withHeader("Content-Disposition", "attachment; filename=" + filename));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Cannot get file."))));
        }
    }

    public CompletionStage<Result> add(Http.Request request, final String organization, final String uuid) {
        Map<String, String[]> values =  request.body().asMultipartFormData().asFormUrlEncoded();

        Http.MultipartFormData<Files.TemporaryFile> body = request.body().asMultipartFormData();
        Http.MultipartFormData.FilePart<Files.TemporaryFile> filePart = body.getFile("file");
        try {
            InputStream inputStream = new FileInputStream(filePart.getRef().path().toFile());
            this.fileService.upload(organization, values.get("objectUuid")[0], values.get("collection")[0], inputStream, filePart.getFilename(), filePart.getFileSize(), values.get("fileType")[0]);
        } catch (FileNotFoundException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Failed to upload file on serve."))));
        }

        AttachmentDao attachment;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            attachment = objectMapper.readValue(values.get("attachment")[0], AttachmentDao.class);
        } catch (IOException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Failed to parse object."))));
        }

        if ( attachment == null || attachment.getFilename() == null || attachment.getVfk() == null) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Attachments filename and vfk are required."))));
        } else {
            attachment.setTenant(organization);
            return attachmentsService.add(attachment, uuid).thenApply(attachmentTmp -> {
                if ( attachmentTmp != null ) {
                    return ok(Json.toJson(new UUIDJson(attachmentTmp.getUuid())));
                } else {
                    return internalServerError(Json.toJson(new ErrorMessage("Error when adding attachment in database")));
                }
            });
        }
    }
}
