package api.v1.controllers;

import accounts.AccountComment;
import accounts.AccountsService;
import api.v1.forms.*;
import core.*;
import core.search.Pageable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

import static java.util.concurrent.CompletableFuture.completedFuture;

public class AccountsController extends Controller {
    protected final static Logger logger = LoggerFactory.getLogger(AccountsController.class);

    private final AccountsService accountsService;
    private final FormFactory formFactory;

    @Inject
    AccountsController(final AccountsService accountsService, final FormFactory formFactory) {
        this.accountsService = accountsService;
        this.formFactory = formFactory;
    }

    private accounts.Account parseAccount(AddAccountForm accountForm) {
        return new accounts.Account(
                Optional.ofNullable(accountForm.getUuid()),
                accountForm.getType(),
                accountForm.getReference(),
                accountForm.getCategory(),
                accountForm.getCommercial().getLogin(),
                accountForm.getContact().getUuid(),
                Optional.ofNullable(accountForm.getImportance()),
                Optional.ofNullable(accountForm.getState()),
                Optional.ofNullable(accountForm.getEntity()).map(AddEntityForm::getUuid),
                Optional.ofNullable(accountForm.getMaxPaymentTime()),
                Optional.empty(),
                Optional.ofNullable(accountForm.getCreated()).isPresent() ? null : new Date()
        );
    }

    public CompletionStage<Result> add(Http.Request request, final String organization) {
        final Form<AddAccountForm> entityForm = formFactory.form(AddAccountForm.class);
        final Form<AddAccountForm> boundForm = entityForm.bindFromRequest(request);
        final Map<String, String[]> entries = request.queryString();
        Optional<String> login = entries.containsKey("login") ? Optional.of(entries.get("login")[0]) : Optional.empty();
        if (boundForm.hasErrors()) {
            return completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> type = Optional.ofNullable(boundForm.get().getType());
            Optional<String> reference = Optional.ofNullable(boundForm.get().getReference());
            Optional<String> category = Optional.ofNullable(boundForm.get().getCategory());
            Optional<String> commercial = Optional.ofNullable(boundForm.get().getCommercial().getLogin());
            Optional<String> state = Optional.ofNullable(boundForm.get().getState());
            Optional<AddPeopleForm> contact = Optional.ofNullable(boundForm.get().getContact());
            if (!type.isPresent() || !reference.isPresent() || !category.isPresent() || !commercial.isPresent() || !state.isPresent() || !contact.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Account type, reference, category, user, state and contact are required."))));
            } else {
                accounts.Account account = parseAccount(boundForm.get());
                return accountsService.add(organization, account, login).thenApply(uuid -> uuid.isPresent()
                        ? ok(Json.toJson(new UUIDJson(uuid.get())))
                        : internalServerError(Json.toJson(new ErrorMessage("Error when adding account in database"))));
            }
        }
    }

    public CompletionStage<Result> get(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Optional<String> suggest = entries.containsKey("suggest") ? Optional.of(entries.get("suggest")[0]) : Optional.empty();
        Optional<String> entity = entries.containsKey("entity") ? Optional.of(entries.get("entity")[0]) : Optional.empty();
        Optional<String> startsWith = entries.containsKey("startsWith") ? Optional.of(entries.get("startsWith")[0]) : Optional.empty();
        Optional<Integer> page = entries.containsKey("page") ? Optional.of(Integer.parseInt(entries.get("page")[0])) : Optional.empty();
        Optional<Integer> rows = entries.containsKey("rows") ? Optional.of(Integer.parseInt(entries.get("rows")[0])) : Optional.empty();
        if (suggest.isPresent()) {
            return accountsService.suggest(organization, suggest.get())
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else if (startsWith.isPresent() && page.isPresent() && rows.isPresent()) {
            return accountsService.searchPage(organization, startsWith.get(), page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else if (startsWith.isPresent()) {
            return accountsService.search(organization, startsWith.get())
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else if (page.isPresent() && rows.isPresent()) {
            return accountsService.getPage(organization, page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else if (entity.isPresent()) {
            return accountsService.getFromEntity(organization, entity.get()).thenCompose(optionalAccount -> {
                if (optionalAccount.isPresent()) {
                    return accountsService.serialize(organization, optionalAccount.get()).thenApply(account -> ok(Json.toJson(account)));
                } else {
                    return completedFuture(badRequest(Json.toJson(new ErrorMessage("No account linked to entity " + entity.get()))));
                }
            });
        } else {
            return accountsService.getAll(organization)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        }
    }

    public CompletionStage<Result> getIndividuals(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Optional<Integer> page = entries.containsKey("page") ? Optional.of(Integer.parseInt(entries.get("page")[0])) : Optional.empty();
        Optional<Integer> rows = entries.containsKey("rows") ? Optional.of(Integer.parseInt(entries.get("rows")[0])) : Optional.empty();
        if (page.isPresent() && rows.isPresent()) {
            return accountsService.getPageIndividuals(organization, page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else {
            return accountsService.getAllIndividuals(organization)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        }
    }

    public CompletionStage<Result> getProfessionals(Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Optional<Integer> page = entries.containsKey("page") ? Optional.of(Integer.parseInt(entries.get("page")[0])) : Optional.empty();
        Optional<Integer> rows = entries.containsKey("rows") ? Optional.of(Integer.parseInt(entries.get("rows")[0])) : Optional.empty();
        if (page.isPresent() && rows.isPresent()) {
            return accountsService.getPageProfessionals(organization, page.get() * rows.get(), rows.get() + 1)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        } else {
            return accountsService.getAllProfessionals(organization)
                    .thenCompose(accounts -> CompletableFutureUtils.sequence(accounts.stream().map(account -> accountsService.serialize(organization, account).toCompletableFuture()).collect(Collectors.toList()))
                            .thenApply(finalAccounts -> ok(Json.toJson(finalAccounts))));
        }
    }

    public CompletionStage<Result> getAccount(final String organization, final String accountId) {
        try {
            return accountsService.get(organization, accountId).thenCompose(account -> {
                if (account.isPresent()) {
                    return accountsService.serialize(organization, account.get()).thenApply(finalAccount -> ok(Json.toJson(finalAccount)));
                } else {
                    return completedFuture(badRequest(Json.toJson(new ErrorMessage("No account with uuid " + accountId + " found"))));
                }
            });
        } catch (IllegalArgumentException e) {
            return completedFuture(badRequest(Json.toJson(new ErrorMessage("The given id is not in the expected format."))));
        }
    }

    public CompletionStage<Result> update(Http.Request request, final String organization, final String uuid) {
        final Form<AddAccountForm> entityForm = formFactory.form(AddAccountForm.class);
        final Form<AddAccountForm> boundForm = entityForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            Optional<String> type = Optional.ofNullable(boundForm.get().getType());
            Optional<String> reference = Optional.ofNullable(boundForm.get().getReference());
            Optional<String> category = Optional.ofNullable(boundForm.get().getCategory());
            Optional<String> commercial = Optional.ofNullable(boundForm.get().getCommercial().getLogin());
            Optional<String> state = Optional.ofNullable(boundForm.get().getState());
            Optional<AddPeopleForm> contact = Optional.ofNullable(boundForm.get().getContact());
            if (!type.isPresent() || !reference.isPresent() || !category.isPresent() || !commercial.isPresent() || !state.isPresent() || !contact.isPresent()) {
                return CompletableFuture.completedFuture(badRequest(Json.toJson(new ErrorMessage("Account type, reference, category, user, state and contact are required."))));
            } else {
                accounts.Account account = parseAccount(boundForm.get());
                return accountsService.update(organization, account, Optional.ofNullable(boundForm.get().getGroups())).thenCompose(sameAccount -> {
                    if (sameAccount.isPresent()) {
                        return accountsService.serialize(organization, sameAccount.get()).thenApply(finalAccount -> ok(Json.toJson(finalAccount)));
                    } else {
                        return CompletableFuture.completedFuture(internalServerError(Json.toJson(new ErrorMessage("Error when updating account " + uuid + " in database"))));
                    }
                });
            }
        }
    }

    public CompletionStage<Result> delete(final String organization, final String uuid) {
        return accountsService.delete(organization, uuid).thenApply(result -> result
                ? ok(Json.toJson(new SuccessMessage("The account has been deleted !")))
                : internalServerError(Json.toJson(new ErrorMessage("Error when deleting account in the database"))));
    }

    public CompletionStage<Result> getProfessionalOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.accountsService.getProfessionalOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list orders overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }

    public CompletionStage<Result> getAccountOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.accountsService.getAccountOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list orders overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }

    public CompletionStage<Result> getIndividualOverviews(final Http.Request request, final String organization) {
        final Map<String, String[]> entries = request.queryString();
        Pageable pageable = new Pageable(entries);

        return this.accountsService.getIndividualOverviews(organization, pageable)
                .thenApply(results -> ok(Json.toJson(results)))
                .exceptionally(t -> {
                    logger.error("Failed to list orders overview.", t);
                    return internalServerError(Json.toJson(new ErrorMessage("Error during listing operation")));
                });
    }

    public CompletionStage<Result> getComments(String organization, String uuid) {
        return accountsService.getComments(organization, uuid)
                .thenCompose(comments -> CompletableFutureUtils.sequence(comments.stream()
                        .map(comment -> accountsService.serializeComment(organization, comment).toCompletableFuture())
                        .collect(Collectors.toList()))
                        .thenApply(finalComments -> ok(Json.toJson(finalComments.stream().filter(Optional::isPresent)))));
    }

    public CompletionStage<Result> addComment(String organization, final Http.Request request) {
        final Form<AddAccountCommentForm> commentForm = formFactory.form(AddAccountCommentForm.class);
        final Form<AddAccountCommentForm> boundForm = commentForm.bindFromRequest(request);
        if (boundForm.hasErrors()) {
            return CompletableFuture.completedFuture(badRequest(boundForm.errorsAsJson()));
        } else {
            return accountsService.addComment(organization,
                    new AccountComment(
                            Optional.empty(),
                            boundForm.get().getIdAccount(),
                            Optional.of(boundForm.get().getUser().getLogin()),
                            boundForm.get().getComment(),
                            new Date(),
                            EventType.MESSAGE)
            ).thenCompose(comment -> {
                if (comment.isPresent()) {
                    return accountsService.serializeComment(organization, comment.get());
                } else {
                    return CompletableFuture.completedFuture(Optional.empty());
                }
            }).thenApply(results -> {
                if (results.isPresent()) {
                    return ok(Json.toJson(results.get()));
                } else {
                    logger.error("Error during adding comment in account");
                    return internalServerError(Json.toJson(new ErrorMessage("Error during adding comment in accounts")));
                }
            });
        }
    }
}
