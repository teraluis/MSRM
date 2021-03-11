package api.v1.controllers;

import agencies.AgenciesService;
import api.v1.forms.AddAgencyForm;
import api.v1.models.Agency;
import core.CompletableFutureUtils;
import core.ErrorMessage;
import core.SuccessMessage;
import core.UUIDJson;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import users.User;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class AgenciesController extends Controller {

    private final AgenciesService agenciesService;
    private final FormFactory formFactory;

    @Inject
    AgenciesController(final AgenciesService agenciesService, final FormFactory formFactory) {
        this.agenciesService = agenciesService;
        this.formFactory = formFactory;
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddAgencyForm> entityForm = formFactory.form(AddAgencyForm.class);
        final Form<AddAgencyForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            Optional<String> optionalManager = Optional.ofNullable(boundForm.get().getManager().getLogin());
            if (!optionalName.isPresent() && !optionalManager.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Agency name and manager are required."))));
            } else {
                Optional<User> manager = Optional.ofNullable(boundForm.get().getManager()).isPresent()
                        ? Optional.of(
                        new User(
                                boundForm.get().getManager().getLogin(),
                                Optional.ofNullable(boundForm.get().getManager().getRegistration_number()),
                                boundForm.get().getManager().getFirst_name(),
                                boundForm.get().getManager().getLast_name(),
                                Optional.ofNullable(boundForm.get().getManager().getOffice()),
                                Optional.ofNullable(boundForm.get().getManager().getPhone()),
                                Optional.ofNullable(boundForm.get().getManager().getDescription())
                        )
                )
                        : Optional.empty();
                agencies.Agency agency = new agencies.Agency(Optional.empty(), boundForm.get().getCode(), boundForm.get().getName(), manager.get().login, new Date(), boundForm.get().getReferenceIban(), boundForm.get().getReferenceBic());
                return agenciesService.add(organization, agency).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding agency in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return agenciesService
                    .searchPage(organization, startsWith, page * rows, rows + 1)
                    .thenCompose(agencies -> CompletableFutureUtils.sequence(agencies.stream().map(agency -> agenciesService.serialize(organization, agency).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAgencies -> ok(Json.toJson(finalAgencies)));
        } else if (startsWith != null) {
            return agenciesService
                    .search(organization, startsWith)
                    .thenCompose(agencies -> CompletableFutureUtils.sequence(agencies.stream().map(agency -> agenciesService.serialize(organization, agency).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAgencies -> ok(Json.toJson(finalAgencies)));
        } else if (page != null && rows != null) {
            return agenciesService
                    .getPage(organization, page * rows, rows + 1)
                    .thenCompose(agencies -> CompletableFutureUtils.sequence(agencies.stream().map(agency -> agenciesService.serialize(organization, agency).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAgencies -> ok(Json.toJson(finalAgencies)));
        } else {
            return agenciesService
                    .getAll(organization)
                    .thenCompose(agencies -> CompletableFutureUtils.sequence(agencies.stream().map(agency -> agenciesService.serialize(organization, agency).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAgencies -> ok(Json.toJson(finalAgencies)));
        }
    }

    public CompletionStage<Result> getAgency(final String organization, final String agencyId) {
        try {
            return agenciesService.get(organization, agencyId).thenCompose(agency -> agency.isPresent()
                    ? agenciesService.serialize(organization, agency.get()).thenApply(finalAgency -> ok(Json.toJson(finalAgency)))
                    : CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No agency with uuid " + agencyId + " found")))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddAgencyForm> entityForm = formFactory.form(AddAgencyForm.class);
        final Form<AddAgencyForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            Optional<String> optionalManager = Optional.ofNullable(boundForm.get().getManager().getLogin());
            if (!optionalName.isPresent() || !optionalManager.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Agency name and manager are required."))));
            } else {
                Optional<User> manager = Optional.ofNullable(boundForm.get().getManager()).isPresent()
                        ? Optional.of(
                        new User(
                                boundForm.get().getManager().getLogin(),
                                Optional.ofNullable(boundForm.get().getManager().getRegistration_number()),
                                boundForm.get().getManager().getFirst_name(),
                                boundForm.get().getManager().getLast_name(),
                                Optional.ofNullable(boundForm.get().getManager().getOffice()),
                                Optional.ofNullable(boundForm.get().getManager().getPhone()),
                                Optional.ofNullable(boundForm.get().getManager().getDescription())
                        )
                )
                        : Optional.empty();
                agencies.Agency agency = new agencies.Agency(Optional.ofNullable(boundForm.get().getUuid()), boundForm.get().getCode(), boundForm.get().getName(), manager.get().login, new Date(), boundForm.get().getReferenceIban(), boundForm.get().getReferenceBic());
                return agenciesService.update(organization, agency).thenCompose(sameAgency -> sameAgency.isPresent()
                        ? agenciesService.serialize(organization, sameAgency.get()).thenApply(finalAgency -> ok(Json.toJson(finalAgency)))
                        : CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating agency " + uuid + " in database")))));
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return agenciesService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The agency has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting agency in the database"))));
    }
}
