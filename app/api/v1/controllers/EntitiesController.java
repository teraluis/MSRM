package api.v1.controllers;

import addresses.Address;
import api.v1.forms.AddAddressForm;
import api.v1.forms.AddEntityForm;
import api.v1.models.AdnParameters;
import core.CompletableFutureUtils;
import core.ErrorMessage;
import core.SuccessMessage;
import core.UUIDJson;
import entities.EntitiesService;
import play.data.Form;
import play.data.FormFactory;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utils.ParsingUtils;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;

public class EntitiesController extends Controller {

    private final EntitiesService entitiesService;
    private final FormFactory formFactory;

    @Inject
    EntitiesController(final EntitiesService entitiesService, final FormFactory formFactory) {
        this.entitiesService = entitiesService;
        this.formFactory = formFactory;
    }

    private entities.Entity parseEntity(AddEntityForm entityForm) {
        return new entities.Entity(
                Optional.ofNullable(entityForm.getUuid()),
                entityForm.getName(),
                entityForm.getCorporateName(),
                Optional.ofNullable(entityForm.getType()),
                entityForm.getSiren(),
                Optional.ofNullable(entityForm.getDomain()),
                Optional.ofNullable(entityForm.getLogo()),
                Optional.ofNullable(entityForm.getDescription()),
                Optional.ofNullable(entityForm.getMainAddress()).isPresent() ? Optional.ofNullable(entityForm.getMainAddress().getUuid()) : Optional.empty(),
                new Date()
        );
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddEntityForm> entityForm = formFactory.form(AddEntityForm.class);
        final Form<AddEntityForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            Optional<String> optionalCorporateName = Optional.ofNullable(boundForm.get().getCorporateName());
            Optional<String> optionalSiren = Optional.ofNullable(boundForm.get().getSiren());
            if (!optionalName.isPresent() || !optionalCorporateName.isPresent() || !optionalSiren.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Entity name, corporate name and siren are required."))));
            } else {
                return entitiesService.add(organization, parseEntity(boundForm.get())).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding entity in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return entitiesService.searchPage(organization, startsWith, page * rows, rows + 1)
                    .thenCompose(entities -> CompletableFutureUtils.sequence(entities.stream().map(entity -> entitiesService.serialize(organization, entity).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEntities -> ok(Json.toJson(finalEntities)));
        } else if (startsWith != null) {
            return entitiesService.search(organization, startsWith)
                    .thenCompose(entities -> CompletableFutureUtils.sequence(entities.stream().map(entity -> entitiesService.serialize(organization, entity).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEntities -> ok(Json.toJson(finalEntities)));
        } else if (page != null && rows != null) {
            return entitiesService.getPage(organization, page * rows, rows + 1)
                    .thenCompose(entities -> CompletableFutureUtils.sequence(entities.stream().map(entity -> entitiesService.serialize(organization, entity).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEntities -> ok(Json.toJson(finalEntities)));
        } else if (entries.containsKey("siren")) {
            return entitiesService.getFromSiren(organization, entries.get("siren")[0]).thenCompose(entity -> entity.isPresent()
                    ? entitiesService.serialize(organization, entity.get()).thenApply(finalEntity -> ok(Json.toJson(finalEntity)))
                    : CompletableFuture.completedFuture(ok(Json.toJson(new SuccessMessage("No entity found with the given siren number.")))));
        } else {
            return entitiesService.getAll(organization)
                    .thenCompose(entities -> CompletableFutureUtils.sequence(entities.stream().map(entity -> entitiesService.serialize(organization, entity).toCompletableFuture()).collect(Collectors.toList())))
                    .thenApply(finalEntities -> ok(Json.toJson(finalEntities)));
        }
    }

    public CompletionStage<Result> getEntity(final String organization, final String entityId) {
        try {
            return entitiesService.get(organization, entityId).thenCompose(entity -> entity.isPresent()
                    ? entitiesService.serialize(organization, entity.get()).thenApply(finalEntity -> ok(Json.toJson(finalEntity)))
                    : CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No entity with uuid " + entityId + " found")))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddEntityForm> entityForm = formFactory.form(AddEntityForm.class);
        final Form<AddEntityForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalName = Optional.ofNullable(boundForm.get().getName());
            Optional<String> optionalCorporateName = Optional.ofNullable(boundForm.get().getCorporateName());
            Optional<String> optionalSiren = Optional.ofNullable(boundForm.get().getSiren());
            if (!optionalName.isPresent() || !optionalCorporateName.isPresent() || !optionalSiren.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Entity name, corporate name and siren are required."))));
            } else {
                return entitiesService.update(organization, parseEntity(boundForm.get())).thenCompose(sameEntity -> sameEntity.isPresent()
                        ? entitiesService.serialize(organization, sameEntity.get()).thenApply(finalEntity -> ok(Json.toJson(finalEntity)))
                        : CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating entity " + uuid + " in database")))));
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return entitiesService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The entity has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting entity in the database"))));
    }

    public CompletionStage<Result> getAdnParameters(Http.Request request, final String organization, final String adnName) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> address1 = Optional.empty();
        Optional<String> address2 = Optional.empty();
        Optional<String> zip = Optional.empty();
        Optional<String> city = Optional.empty();
        if (entries.containsKey("address1")) {
            address1 = Optional.of(entries.get("address1")[0]);
        }
        if (entries.containsKey("address2")) {
            address2 = Optional.of(entries.get("address2")[0]);
        }
        if (entries.containsKey("zip")) {
            zip = Optional.of(entries.get("zip")[0]);
        }
        if (entries.containsKey("city")) {
            city = Optional.of(entries.get("city")[0]);
        }
        return entitiesService.getAdnParameters(organization, adnName, address1, address2, zip, city).thenCompose(adnParameters -> {
            if (adnParameters.isPresent()) {
                return entitiesService.get(organization, adnParameters.get().entity).thenCompose(entity -> {
                    if (entity.isPresent()) {
                        return entitiesService.serialize(organization, entity.get())
                                .thenApply(finalEntity -> ok(Json.toJson(AdnParameters.serialize(adnParameters.get(), finalEntity))));
                    } else {
                        return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("No account for adn parameter " + adnParameters.get().adnId))));
                    }
                });
            } else {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("No adn parameters with matching name and address found"))));
            }
        });
    }
}
