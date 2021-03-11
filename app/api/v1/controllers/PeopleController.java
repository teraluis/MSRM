package api.v1.controllers;

import addresses.AddressesService;
import api.v1.forms.AddPeopleForm;
import core.CompletableFutureUtils;
import core.ErrorMessage;
import core.SuccessMessage;
import core.UUIDJson;
import core.models.AddressWithRole;
import people.PeopleService;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class PeopleController extends Controller {

    protected final PeopleService peopleService;
    protected final AddressesService addressesService;
    private final FormFactory formFactory;

    @Inject
    PeopleController(final PeopleService peopleService, AddressesService addressesService, final FormFactory formFactory) {
        this.peopleService = peopleService;
        this.addressesService = addressesService;
        this.formFactory = formFactory;
    }

    private people.People parsePeople(AddPeopleForm peopleForm) {
        return new people.People(
                Optional.ofNullable(peopleForm.getUuid()),
                peopleForm.getTitle(),
                peopleForm.getLastname(),
                peopleForm.getFirstname(),
                Optional.ofNullable(peopleForm.getWorkMail()),
                Optional.ofNullable(peopleForm.getEmail()),
                Optional.ofNullable(peopleForm.getWorkPhone()),
                Optional.ofNullable(peopleForm.getMobilePhone()),
                Optional.ofNullable(peopleForm.getJobDescription())
        );
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddPeopleForm> peopleForm = formFactory.form(AddPeopleForm.class);
        final Form<AddPeopleForm> boundForm = peopleForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalTitle = Optional.ofNullable(boundForm.get().getTitle());
            Optional<String> optionalLastname = Optional.ofNullable(boundForm.get().getLastname());
            Optional<String> optionalFirstname = Optional.ofNullable(boundForm.get().getFirstname());
            if (!optionalTitle.isPresent() || !optionalLastname.isPresent() && !optionalFirstname.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("People title, firstname and lastname are required."))));
            } else {
                return peopleService.add(organization, parsePeople(boundForm.get())).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding people in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return peopleService.searchPage(organization, startsWith, page * rows, rows + 1)
                    .thenCompose(peoples -> CompletableFutureUtils.sequence(peoples.stream().map(people -> peopleService.serialize(organization, people).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalPeople -> ok(Json.toJson(finalPeople)));
        } else if (startsWith != null) {
            return peopleService.search(organization, startsWith)
                    .thenCompose(peoples -> CompletableFutureUtils.sequence(peoples.stream().map(people -> peopleService.serialize(organization, people).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalPeople -> ok(Json.toJson(finalPeople)));
        } else if (page != null && rows != null) {
            return peopleService.getPage(organization, page * rows, rows + 1)
                    .thenCompose(peoples -> CompletableFutureUtils.sequence(peoples.stream().map(people -> peopleService.serialize(organization, people).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalPeople -> ok(Json.toJson(finalPeople)));
        } else {
            return peopleService.getAll(organization)
                    .thenCompose(peoples -> CompletableFutureUtils.sequence(peoples.stream().map(people -> peopleService.serialize(organization, people).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalPeople -> ok(Json.toJson(finalPeople)));
        }

    }

    public CompletionStage<Result> getPeople(final String organization, final String uuid) {
        return peopleService.get(organization, uuid).thenCompose(people -> {
            if (people.isPresent()) {
                return peopleService.serialize(organization, people.get()).thenApply(finalPeople -> ok(Json.toJson(finalPeople)));
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No people with uuid " + uuid + " found in organization " + organization))));
            }
        });
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddPeopleForm> peopleForm = formFactory.form(AddPeopleForm.class);
        final Form<AddPeopleForm> boundForm = peopleForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return peopleService.update(organization, parsePeople(boundForm.get())).thenCompose(samePeople -> samePeople.isPresent()
                    ? peopleService.serialize(organization, samePeople.get()).thenApply(finalPeople -> ok(Json.toJson(finalPeople)))
                    : CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating people " + uuid + " in database")))));
        }
    }

    public CompletionStage<Result> getPeopleAddresses(Http.Request request, final String organization, final String peopleId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return peopleService.getAddressesByRole(organization, peopleId, entries.get("role")[0])
                    .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address).thenApply(address -> new AddressWithRole(address.get().serialize(), addressWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAddresses -> ok(Json.toJson(finalAddresses)));
        } else {
            return peopleService.getAddresses(organization, peopleId)
                    .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address).thenApply(address -> new AddressWithRole(address.get().serialize(), addressWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAddresses -> ok(Json.toJson(finalAddresses)));
        }
    }

    public CompletionStage<Result> addPeopleAddress(Http.Request request, final String organization, final String peopleId, final String addressId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return peopleService.addAddress(peopleId, addressId, entries.get("role")[0]).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Address has been linked to people !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when linking address to people in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> removePeopleAddress(Http.Request request, final String organization, final String peopleId, final String addressId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return peopleService.removeAddress(peopleId, addressId, entries.get("role")[0]).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Link between address and people has been removed !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when removing link between address and people in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> getPurchasers(Http.Request request, final String organization, final String uuid) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> market = entries.containsKey("market") ? Optional.of(entries.get("market")[0]) : Optional.empty();
        return peopleService.getPurchasers(organization, uuid, market).thenApply(peopleWithOrigins -> ok(Json.toJson(peopleWithOrigins)));
    }
}
