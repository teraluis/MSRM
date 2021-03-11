package api.v1.controllers;

import api.v1.forms.AddGroupForm;
import api.v1.models.Group;
import core.ErrorMessage;
import core.SuccessMessage;
import core.UUIDJson;
import groups.GroupsService;
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

public class GroupsController extends Controller {

    private final GroupsService groupsService;
    private final FormFactory formFactory;

    @Inject
    GroupsController(final GroupsService groupsService, final FormFactory formFactory) {
        this.groupsService = groupsService;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddGroupForm> entityForm = formFactory.form(AddGroupForm.class);
        final Form<AddGroupForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            Optional<String> optionalType = Optional.ofNullable(boundForm.get().getType());
            if (!optionalName.isPresent() || !optionalType.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Group name and type are required."))));
            } else {
                groups.Group group = new groups.Group(Optional.empty(), boundForm.get().getName(), boundForm.get().getType(),
                        Optional.ofNullable(boundForm.get().getCategory()), Optional.ofNullable(boundForm.get().getIban()), Optional.ofNullable(boundForm.get().getDescription()), new Date());
                return groupsService.add(organization, group).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding group in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return groupsService.searchPage(organization, startsWith, page * rows, rows + 1).thenApply(groups -> ok(Json.toJson(groups.stream().map(Group::serialize))));
        } else if (startsWith != null) {
            return groupsService.search(organization, startsWith).thenApply(groups -> ok(Json.toJson(groups.stream().map(Group::serialize))));
        } else if (page != null && rows != null) {
            return groupsService.getPage(organization, page * rows, rows + 1).thenApply(groups -> ok(Json.toJson(groups.stream().map(Group::serialize))));
        } else if (entries.containsKey("account")) {
            return groupsService.getFromAccount(organization, entries.get("account")[0]).thenApply(groups -> ok(Json.toJson(groups.stream().map(Group::serialize))));
        } else {
            return groupsService.getAll(organization).thenApply(groups -> ok(Json.toJson(groups.stream().map(Group::serialize))));
        }
    }

    public CompletionStage<Result> getGroup(final String organization, final String groupId) {
        try {
            return groupsService.get(organization, groupId).thenApply(group -> group.isPresent()
                    ? ok(Json.toJson(Group.serialize(group.get())))
                    : badRequest(Json.toJson(new ErrorMessage("No group with uuid " + groupId + " found"))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddGroupForm> entityForm = formFactory.form(AddGroupForm.class);
        final Form<AddGroupForm> boundForm = entityForm.bindFromRequest(request);
        Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
        Optional<String> optionalType = Optional.ofNullable(boundForm.get().getType());
        if (!optionalName.isPresent() || !optionalType.isPresent()) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Group name and type are required."))));
        } else {
            groups.Group group = new groups.Group(Optional.ofNullable(boundForm.get().getUuid()), boundForm.get().getName(), boundForm.get().getType(),
                    Optional.ofNullable(boundForm.get().getCategory()), Optional.ofNullable(boundForm.get().getIban()), Optional.ofNullable(boundForm.get().getDescription()), new Date());
            return groupsService.update(organization, group).thenApply(sameGroup -> sameGroup.isPresent()
                    ? ok(Json.toJson(Group.serialize(sameGroup.get())))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when updating group " + uuid + " in database"))));
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return groupsService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The group has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting group in the database"))));
    }
}
