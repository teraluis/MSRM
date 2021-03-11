package api.v1.controllers;

import activities.ActivitiesService;
import api.v1.forms.AddActivityForm;
import api.v1.models.Activity;
import core.ErrorMessage;
import core.SuccessMessage;
import core.UUIDJson;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class ActivitiesController extends Controller {

    private final ActivitiesService activitiesService;
    private final FormFactory formFactory;

    @Inject
    ActivitiesController(final ActivitiesService activitiesService, final FormFactory formFactory) {
        this.activitiesService = activitiesService;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddActivityForm> entityForm = formFactory.form(AddActivityForm.class);
        final Form<AddActivityForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            if (!optionalName.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Activity name is required."))));
            } else {
                activities.Activity activity = new activities.Activity(Optional.empty(), boundForm.get().getName(), Optional.ofNullable(boundForm.get().getDescription()), new Date());
                return activitiesService.add(organization, activity).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding activity in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return activitiesService.searchPage(organization, startsWith, page * rows, rows + 1).thenApply(activities -> ok(Json.toJson(activities.stream().map(Activity::serialize))));
        } else if (startsWith != null) {
            return activitiesService.search(organization, startsWith).thenApply(activities -> ok(Json.toJson(activities.stream().map(Activity::serialize))));
        } else if (page != null && rows != null) {
            return activitiesService.getPage(organization, page * rows, rows + 1).thenApply(activities -> ok(Json.toJson(activities.stream().map(Activity::serialize))));
        } else {
            return activitiesService.getAll(organization).thenApply(activities -> ok(Json.toJson(activities.stream().map(Activity::serialize))));
        }
    }

    public CompletionStage<Result> getActivity(final String organization, final String activityId) {
        try {
            return activitiesService.get(organization, activityId).thenApply(activity -> activity.isPresent()
                    ? ok(Json.toJson(Activity.serialize(activity.get())))
                    : badRequest(Json.toJson(new ErrorMessage("No activity with uuid " + activityId + " found"))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        Map<String, String[]> bodyForm = request.body().asMultipartFormData().asFormUrlEncoded();
        if (!bodyForm.containsKey("name")) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Activity name is required."))));
        } else {
            String optionalName = bodyForm.get("name")[0];
            Optional<String> description = bodyForm.containsKey("description") ? Optional.of(bodyForm.get("description")[0]) : Optional.empty();
            activities.Activity activity = new activities.Activity(Optional.of(uuid), optionalName, description, null);
            return activitiesService.update(organization, activity).thenApply(sameActivity -> sameActivity.isPresent()
                    ? ok(Json.toJson(Activity.serialize(sameActivity.get())))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when updating activity " + uuid + " in database"))));
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return activitiesService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The activity has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting activity in the database"))));
    }
}
