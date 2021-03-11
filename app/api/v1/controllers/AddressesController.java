package api.v1.controllers;

import addresses.Address;
import addresses.AddressesService;
import api.v1.forms.AddAddressForm;

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

public class AddressesController extends Controller {

    private final AddressesService addressesService;
    private final FormFactory formFactory;

    @Inject
    AddressesController(final AddressesService addressesService, final FormFactory formFactory) {
        this.addressesService = addressesService;
        this.formFactory = formFactory;
    }

    private addresses.Address parseAddress(AddAddressForm addressForm) {
        return new addresses.Address(
                Optional.ofNullable(addressForm.getUuid()),
                addressForm.getType(),
                Optional.ofNullable(addressForm.getAddress1()),
                Optional.ofNullable(addressForm.getAddress2()),
                Optional.ofNullable(addressForm.getPostCode()),
                Optional.ofNullable(addressForm.getCity()),
                Optional.ofNullable(addressForm.getGpsCoordinates()),
                Optional.ofNullable(addressForm.getInseeCoordinates()),
                Optional.ofNullable(addressForm.getDispatch()),
                Optional.ofNullable(addressForm.getStaircase()),
                Optional.ofNullable(addressForm.getWayType()),
                Optional.ofNullable(addressForm.getCountry()),
                new Date()
        );
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddAddressForm> entityForm = formFactory.form(AddAddressForm.class);
        final Form<AddAddressForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalType = Optional.ofNullable(boundForm.get().getType());
            Optional<String> optionalAddress = Optional.ofNullable(boundForm.get().getAddress1());
            Optional<String> optionalPostCode = Optional.ofNullable(boundForm.get().getPostCode());
            Optional<String> optionalCity = Optional.ofNullable(boundForm.get().getCity());
            Optional<String> optionalGpsCoordinates = Optional.ofNullable(boundForm.get().getGpsCoordinates());
            Optional<String> optionalInseeCoordinates = Optional.ofNullable(boundForm.get().getInseeCoordinates());
            if (!optionalType.isPresent() || !((optionalAddress.isPresent() && optionalPostCode.isPresent() && optionalCity.isPresent()) || optionalGpsCoordinates.isPresent() || optionalInseeCoordinates.isPresent())) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Address type and a location are required."))));
            } else {
                return addressesService.add(organization, parseAddress(boundForm.get())).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding address in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        String startsWith = entries.containsKey("startsWith") ? entries.get("startsWith")[0] : null;
        Integer page = entries.containsKey("page") ? Integer.parseInt(entries.get("page")[0]) : null;
        Integer rows = entries.containsKey("rows") ? Integer.parseInt(entries.get("rows")[0]) : null;
        if (startsWith != null && page != null && rows != null) {
            return addressesService.searchPage(organization, startsWith, page * rows, rows + 1).thenApply(addresses -> ok(Json.toJson(addresses.stream().map(Address::serialize))));
        } else if (startsWith != null) {
            return addressesService.search(organization, startsWith).thenApply(addresses -> ok(Json.toJson(addresses.stream().map(Address::serialize))));
        } else if (page != null && rows != null) {
            return addressesService.getPage(organization, page * rows, rows + 1).thenApply(addresses -> ok(Json.toJson(addresses.stream().map(Address::serialize))));
        } else {
            return addressesService.getAll(organization).thenApply(addresses -> ok(Json.toJson(addresses.stream().map(Address::serialize))));
        }
    }

    public CompletionStage<Result> getAddress(final String organization, final String addressId) {
        try {
            return addressesService.get(organization, addressId).thenApply(address -> address.isPresent()
                    ? ok(Json.toJson(address.get().serialize()))
                    : badRequest(Json.toJson(new ErrorMessage("No address with uuid " + addressId + " found"))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddAddressForm> entityForm = formFactory.form(AddAddressForm.class);
        final Form<AddAddressForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> optionalType = Optional.ofNullable(boundForm.get().getType());
            Optional<String> optionalAddress = Optional.ofNullable(boundForm.get().getAddress1());
            Optional<String> optionalPostCode = Optional.ofNullable(boundForm.get().getPostCode());
            Optional<String> optionalCity = Optional.ofNullable(boundForm.get().getCity());
            Optional<String> optionalGpsCoordinates = Optional.ofNullable(boundForm.get().getGpsCoordinates());
            Optional<String> optionalInseeCoordinates = Optional.ofNullable(boundForm.get().getInseeCoordinates());
            if (!optionalType.isPresent() || !((optionalAddress.isPresent() && optionalPostCode.isPresent() && optionalCity.isPresent()) || optionalGpsCoordinates.isPresent() || optionalInseeCoordinates.isPresent())) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Address type and a location are required."))));
            } else {
                return addressesService.update(organization, parseAddress(boundForm.get())).thenApply(sameAddress -> sameAddress.isPresent()
                        ? ok(Json.toJson(sameAddress.get().serialize()))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when updating address " + uuid + " in database"))));
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return addressesService.delete(organization, uuid).thenApply(result -> result.isPresent()
                ? ok(Json.toJson(new SuccessMessage("The address has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting address in the database"))));
    }
}
