package api.v1.controllers;

import core.ErrorMessage;
import core.UUIDJson;
import models.OfficeDao;
import office.OfficeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static java.util.concurrent.CompletableFuture.completedFuture;

public class OfficeController extends Controller {
    protected static final Logger logger = LoggerFactory.getLogger(OfficeController.class);

    private final OfficeService officeService;

    @Inject
    OfficeController(final OfficeService officeService) {
        this.officeService = officeService;
    }

    public CompletionStage<Result> get(Http.Request request, String organization) {
        return officeService.getAll().thenApply(officies -> ok(Json.toJson(officies)));
    }

    public CompletionStage<Result> getOffice(final String organization, final String uuid) {
        return officeService.getOne(uuid).thenCompose(office -> {
            if (office != null) {
                return completedFuture(ok(Json.toJson(office)));
            } else {
                return completedFuture(badRequest(Json.toJson(new ErrorMessage("No office with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> getOfficiesByAgencyId(final String organization, final String uuid) {
        return officeService.getByAgencyId(uuid).thenCompose(officies -> {
            if (officies != null) {
                return completedFuture(ok(Json.toJson(officies)));
            } else {
                return completedFuture(badRequest(Json.toJson(new ErrorMessage("No officies found with uuid " + uuid + " in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> search(final String organization, final String value) {
        try {
            return officeService.searchByName(value).thenApply(officies -> ok(Json.toJson(officies)));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error while getting officies"))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization) {
        Optional<OfficeDao> officeDao = request.body().parseJson(OfficeDao.class);
        return officeService.update(officeDao.get()).thenApply(officeTmp -> {
            if ( officeTmp != null ) {
                return ok(Json.toJson(new UUIDJson(officeTmp.getUuid())));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Error when adding attachment in database")));
            }
        });
    }
}
