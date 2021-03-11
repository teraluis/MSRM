package api.v1.controllers;

import addresses.AddressesService;
import api.v1.forms.AddActivityForm;
import api.v1.forms.AddEstablishmentCommentForm;
import api.v1.forms.AddEstablishmentForm;
import api.v1.models.EstablishmentWithRole;
import core.*;
import core.models.AddressWithRole;
import core.models.PeopleWithRole;
import core.search.Pageable;
import establishments.Establishment;
import establishments.EstablishmentComment;
import establishments.EstablishmentsService;
import nl.garvelink.iban.IBAN;
import nl.garvelink.iban.Modulo97;
import orders.OrdersService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import people.PeopleService;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class EstablishmentsController extends Controller {
    protected final static Logger logger = LoggerFactory.getLogger(EstablishmentsController.class);

    private final EstablishmentsService establishmentsService;
    private final AddressesService addressesService;
    private final PeopleService peopleService;
    private final OrdersService ordersService;
    private final FormFactory formFactory;

    @Inject
    EstablishmentsController(final EstablishmentsService establishmentsService, final AddressesService addressesService, final PeopleService peopleService, final OrdersService ordersService, final FormFactory formFactory) {
        this.establishmentsService = establishmentsService;
        this.addressesService = addressesService;
        this.peopleService = peopleService;
        this.ordersService = ordersService;
        this.formFactory = formFactory;
    }

    private establishments.Establishment parseEstablishment(AddEstablishmentForm establishmentForm, String sageCode, Boolean clientExported, Boolean validatorExported) {
        return new establishments.Establishment(
                Optional.ofNullable(establishmentForm.getUuid()),
                establishmentForm.getName(),
                establishmentForm.getCorporateName(),
                establishmentForm.getSiret(),
                sageCode,
                Optional.ofNullable(establishmentForm.getDescription()),
                Optional.ofNullable(establishmentForm.getMail()),
                Optional.ofNullable(establishmentForm.getPhone()),
                Optional.ofNullable(establishmentForm.getActivity()).map(AddActivityForm::getUuid),
                establishmentForm.getEntity(),
                Optional.ofNullable(establishmentForm.getCreated()).isPresent() ? null : new Date(),
                false, // exported value not used by repository
                clientExported, // establishement is considered modified if already exported
                false, // exported value not used by repository
                validatorExported, // establishement is considered modified if already exported
                establishmentForm.getIban(),
                establishmentForm.getBic(),
                establishmentForm.getFacturationAnalysis(),
                establishmentForm.getAgency().getUuid()
        );
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddEstablishmentForm> establishmentForm = formFactory.form(AddEstablishmentForm.class);
        final Form<AddEstablishmentForm> boundForm = establishmentForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> sageCodeFromModel = Optional.ofNullable(boundForm.get().getSageCode());
            if (sageCodeFromModel.isPresent()) {
                Establishment establishment = parseEstablishment(boundForm.get(), sageCodeFromModel.get(), false, false);
                return establishmentsService.add(organization, establishment, login).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding establishment in database"))));
            } else {
                return establishmentsService.getSageCode(organization).thenCompose(sageCode -> {
                    Establishment establishment = parseEstablishment(boundForm.get(), sageCode, false, false);
                    return establishmentsService.add(organization, establishment, login).thenApply(uuid -> uuid.isPresent()
                            ? ok(Json.toJson(new UUIDJson(uuid.get())))
                            : internalServerError(Json.toJson(new ErrorMessage("Error when adding establishment in database"))));
                });
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return establishmentsService.searchPage(organization, startsWith, page * rows, rows + 1)
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serialize(organization, establishment).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        } else if (startsWith != null) {
            return establishmentsService.search(organization, startsWith)
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serialize(organization, establishment).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        } else if (page != null && rows != null) {
            return establishmentsService.getPage(organization, page * rows, rows + 1)
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serialize(organization, establishment).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        } else if (entries.containsKey("suggestfororder")) {
            return ordersService.getEstablishmentsWithOrder(organization).thenCompose(establishmentWithOrders -> establishmentsService.search(organization, entries.get("suggestfororder")[0])
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serializeFull(organization, establishment, establishmentWithOrders.contains(establishment.uuid)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(fullEstablishments -> ok(Json.toJson(fullEstablishments))));
        } else if (entries.containsKey("entity")) {
            return ordersService.getEstablishmentsWithOrder(organization).thenCompose(establishmentsWithOrder -> establishmentsService.getFromEntity(organization, entries.get("entity")[0])
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serializeFull(organization, establishment, establishmentsWithOrder.contains(establishment.uuid)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments))));
        } else if (entries.containsKey("siret")) {
            return establishmentsService.getFromSiret(organization, entries.get("siret")[0]).thenCompose(establishment -> establishment.isPresent()
                    ? establishmentsService.serialize(organization, establishment.get()).thenApply(finalEstablishment -> ok(Json.toJson(finalEstablishment)))
                    : CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("No establishment found with the given siret number.")))));
        } else {
            return establishmentsService.getAll(organization)
                    .thenCompose(establishments -> CompletableFutureUtils.sequence(establishments.stream().map(establishment -> establishmentsService.serialize(organization, establishment).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        }
    }

    public CompletionStage<Result> getEstablishment(final String organization, final String establishmentId) {
        try {
            return establishmentsService.get(organization, establishmentId).thenCompose(establishment -> establishment.isPresent()
                    ? establishmentsService.serialize(organization, establishment.get()).thenApply(finalEstablishment -> ok(Json.toJson(finalEstablishment)))
                    : CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No establishment with uuid " + establishmentId + " found")))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> getFullEstablishment(final String organization, final String establishmentId) {
        try {
            return ordersService.getEstablishmentsWithOrder(organization).thenCompose(establishmentsWithOrder -> establishmentsService.get(organization, establishmentId).thenCompose(establishment -> establishment.isPresent()
                    ? establishmentsService.serializeFull(organization, establishment.get(), establishmentsWithOrder.contains(establishmentId)).thenApply(finalEstablishment -> ok(Json.toJson(finalEstablishment)))
                    : CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No establishment with uuid " + establishmentId + " found"))))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddEstablishmentForm> establishmentForm = formFactory.form(AddEstablishmentForm.class);
        final Form<AddEstablishmentForm> boundForm = establishmentForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            IBAN iban = IBAN.valueOf(boundForm.get().getIban());
            if (!Modulo97.verifyCheckDigits(iban.toPlainString())) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Wrong IBAN format"))));
            } else {
                return establishmentsService.get(organization, uuid).thenCompose(existingEstablishment -> {
                    if (existingEstablishment.isPresent()) {
                        // No need for sage code, we don't update it
                        return establishmentsService.update(organization, parseEstablishment(boundForm.get(), "", existingEstablishment.get().clientExported, existingEstablishment.get().validatorExported), login).thenCompose(sameEstablishment -> sameEstablishment.isPresent()
                                ? establishmentsService.serializeFull(organization, sameEstablishment.get(), false).thenApply(finalEstablishment -> ok(Json.toJson(finalEstablishment)))
                                : CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating establishment " + uuid + " in database")))));
                    } else {
                        return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No establishment " + uuid + " in database"))));
                    }
                });
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return establishmentsService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The establishment has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting establishment in the database"))));
    }

    public CompletionStage<Result> getEstablishmentDelegates(Http.Request request, final String organization, final String establishmentId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return establishmentsService.getDelegatesByRole(organization, establishmentId, entries.get("role")[0])
                    .thenCompose(establishmentWithRoles -> CompletableFutureUtils.sequence(establishmentWithRoles.stream().map(establishmentWithRole -> establishmentsService.get(organization, establishmentWithRole.establishment).thenCompose(establishment -> establishmentsService.serialize(organization, establishment.get())).thenApply(serializedEstablishment -> EstablishmentWithRole.serialize(serializedEstablishment, establishmentWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        } else {
            return establishmentsService.getDelegates(organization, establishmentId)
                    .thenCompose(establishmentWithRoles -> CompletableFutureUtils.sequence(establishmentWithRoles.stream().map(establishmentWithRole -> establishmentsService.get(organization, establishmentWithRole.establishment).thenCompose(establishment -> establishmentsService.serialize(organization, establishment.get())).thenApply(serializedEstablishment -> EstablishmentWithRole.serialize(serializedEstablishment, establishmentWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEstablishments -> ok(Json.toJson(finalEstablishments)));
        }
    }

    public CompletionStage<Result> getEstablishmentAddresses(Http.Request request, final String organization, final String establishmentId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return establishmentsService.getAddressesByRole(organization, establishmentId, entries.get("role")[0])
                    .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address).thenApply(address -> new AddressWithRole(address.get().serialize(), addressWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAddresses -> ok(Json.toJson(finalAddresses)));
        } else {
            return establishmentsService.getAddresses(organization, establishmentId)
                    .thenCompose(addressWithRoles -> CompletableFutureUtils.sequence(addressWithRoles.stream().map(addressWithRole -> addressesService.get(organization, addressWithRole.address).thenApply(address -> new AddressWithRole(address.get().serialize(), addressWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAddresses -> ok(Json.toJson(finalAddresses)));
        }
    }

    public CompletionStage<Result> getEstablishmentPeople(Http.Request request, final String organization, final String establishmentId) {
        final Map<String, String[]> entries = request.queryString();
        if (entries.containsKey("role")) {
            return establishmentsService.getPeopleByRole(organization, establishmentId, entries.get("role")[0])
                    .thenCompose(peopleWithRoles -> CompletableFutureUtils.sequence(peopleWithRoles.stream().map(peopleWithRole -> peopleService.get(organization, peopleWithRole.people).thenCompose(people -> peopleService.serialize(organization, people.get())).thenApply(serializedPeople -> new PeopleWithRole(serializedPeople, peopleWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAccount -> ok(Json.toJson(finalAccount)));
        } else {
            return establishmentsService.getPeople(organization, establishmentId)
                    .thenCompose(peopleWithRoles -> CompletableFutureUtils.sequence(peopleWithRoles.stream().map(peopleWithRole -> peopleService.get(organization, peopleWithRole.people).thenCompose(people -> peopleService.serialize(organization, people.get())).thenApply(serializedPeople -> new PeopleWithRole(serializedPeople, peopleWithRole.role)).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalAccount -> ok(Json.toJson(finalAccount)));
        }
    }

    public CompletionStage<Result> addEstablishmentDelegate(Http.Request request, final String organization, final String establishmentId, final String delegateId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.addDelegate(organization, establishmentId, delegateId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Delegate has been linked to establishment !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when linking delegate to establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> addEstablishmentAddress(Http.Request request, final String organization, final String establishmentId, final String addressId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.addAddress(organization, establishmentId, addressId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Address has been linked to establishment !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when linking address to establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> addEstablishmentPeople(Http.Request request, final String organization, final String establishmentId, final String peopleId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.addPeople(organization, establishmentId, peopleId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("People has been linked to establishment !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when linking people to establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> removeEstablishmentDelegate(Http.Request request, final String organization, final String establishmentId, final String delegateId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.removeDelegate(organization, establishmentId, delegateId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Link between delegate and establishment has been removed !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when removing link between delegate and establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> removeEstablishmentAddress(Http.Request request, final String organization, final String establishmentId, final String addressId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.removeAddress(organization, establishmentId, addressId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Link between address and establishment has been removed !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when removing link between address and establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }

    public CompletionStage<Result> removeEstablishmentPeople(Http.Request request, final String organization, final String establishmentId, final String peopleId) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (entries.containsKey("role")) {
            return establishmentsService.removePeople(organization, establishmentId, peopleId, entries.get("role")[0], login).thenApply(resp -> resp
                    ? ok(Json.toJson(new SuccessMessage("Link between people and establishment has been removed !")))
                    : internalServerError(Json.toJson(new ErrorMessage("Error when removing link between people and establishment in database"))));
        } else {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Role is required."))));
        }
    }


    public CompletionStage<Result> getOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.establishmentsService.getOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list establishments overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }

    public CompletionStage<Result> getComments(String organization, String uuid) {
        /* comments without user are filtered out */
        return establishmentsService.getComments(organization, uuid)
                .thenCompose(comments -> CompletableFutureUtils.sequence(comments.stream()
                        .map(comment -> establishmentsService.serializeComment(organization, comment).toCompletableFuture())
                        .collect(Collectors.toList()))
                        .thenApply(finalComments -> ok(Json.toJson(finalComments.stream().filter(Optional::isPresent).collect(Collectors.toList())))));
    }

    public CompletionStage<Result> addComment(String organization, Http.Request request) {
        final Form<AddEstablishmentCommentForm> commentForm = formFactory.form(AddEstablishmentCommentForm.class);
        final Form<AddEstablishmentCommentForm> boundForm = commentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return establishmentsService.addComment(organization,
                    new EstablishmentComment(
                            Optional.empty(),
                            boundForm.get().getIdEstablishment(),
                            Optional.of(boundForm.get().getUser().getLogin()),
                            boundForm.get().getComment(),
                            new Date(),
                            EventType.MESSAGE))
                    .thenCompose(comment -> {
                        if (comment.isPresent()) {
                            return establishmentsService.serializeComment(organization, comment.get());
                        } else {
                            return CompletableFuture.completedFuture(Optional.empty());
                        }
                    })
                    .thenApply(results -> {
                        if (results.isPresent()) {
                            return ok(Json.toJson(results.get()));
                        } else {
                            logger.error("Error during adding comment in Establishment");
                            return internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in Establishment")));
                        }
                    }).exceptionally(t -> internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in Establishment " + t.getMessage()))));
        }
    }
}

