package api.v1.controllers;

import api.v1.forms.PatchEstimateForm;
import core.ErrorMessage;
import core.SuccessMessage;
import estimates.EstimatesService;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class EstimatesController extends Controller {

    protected final EstimatesService estimatesService;
    protected final FormFactory formFactory;

    @Inject
    EstimatesController(final EstimatesService estimatesService, final FormFactory formFactory) {
        this.estimatesService = estimatesService;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        return CompletableFuture.completedFuture(paymentRequired());
    }

    public CompletionStage<Result> getEstimate(final String organization, final String uuid) {
        return estimatesService.getFullEstimate(organization, uuid).thenApply(estimate -> {
            if (estimate.isPresent()) {
                return ok(Json.toJson(estimate.get()));
            } else {
                return badRequest(Json.toJson(new ErrorMessage("No estimate with uuid " + uuid + " found in organization " + organization)));
            }
        });
    }

    public CompletionStage<Result> modify(Http.Request request, final String organization, final String uuid) {
        final Form<PatchEstimateForm> estimateForm = formFactory.form(PatchEstimateForm.class);
        final Form<PatchEstimateForm> boundForm = estimateForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            try {
                Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
                if (!optionalName.isPresent()) {
                    return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Estimate name is required."))));
                } else {
                    return estimatesService.patch(organization, uuid, boundForm.get().getName(), Optional.ofNullable(boundForm.get().getMarket()), Optional.ofNullable(boundForm.get().getAccount())).thenApply(result -> {
                        if (result) {
                            return ok(Json.toJson(new SuccessMessage("The estimate has been patched !")));
                        } else {
                            return internalServerError(Json.toJson(new ErrorMessage("Error when patching estimate in the database")));
                        }
                    });
                }
            } catch (IllegalArgumentException e) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Invalid uuid input " + boundForm.get().getMarket()))));
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return estimatesService.delete(organization, uuid).thenApply(result -> {
            if (result.isPresent()) {
                return ok(Json.toJson(new SuccessMessage("The estimate has been delete !")));
            } else {
                return internalServerError(Json.toJson(new ErrorMessage("Error when deleting estimate in the database")));
            }
        });
    }

}
