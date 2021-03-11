package api.v1.controllers;

import core.ErrorMessage;
import play.libs.Json;
import play.mvc.Result;
import users.UsersService;

import javax.inject.Inject;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import static play.mvc.Results.*;

public class UsersController {

    private final UsersService usersService;

    @Inject
    UsersController(final UsersService usersService) {
        this.usersService = usersService;
    }

    public CompletionStage<Result> getUserWithGroups(final String organization) {
        try {
            return usersService.getUserWithGroups(organization).thenApply(users -> ok(Json.toJson(users)));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error while getting user with groups.@@"))));
        }
    }

    public CompletionStage<Result> getUser(final String organization, final String userId) {
        try {
            return usersService.get(organization, userId).thenApply(user -> user.isPresent()
                    ? ok(Json.toJson(user.get()))
                    : badRequest(Json.toJson(new ErrorMessage("No user with login " + userId + " found"))));
        } catch (IllegalArgumentException e) {
            return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> search(final String organization, final String value) {
        try {
            return usersService.searchByFirstNameAndLastName(organization, value).thenApply(users -> ok(Json.toJson(users)));
        } catch (Exception e) {
            return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error while getting users"))));
        }
    }
}
