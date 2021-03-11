package accounts;

import core.search.Pageable;
import core.search.PaginatedResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AccountsService {

    CompletionStage<Optional<String>> add(String organization, Account account, Optional<String> login);

    CompletionStage<Optional<Account>> update(String organization, Account account, Optional<List<String>> groups);

    CompletionStage<Optional<Account>> get(String organization, String uuid);

    CompletionStage<Optional<Account>> getFromEntity(String organization, String entityId);

    CompletionStage<List<Account>> getAll(String organization);

    CompletionStage<List<Account>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Account>> getAllIndividuals(String organization);

    CompletionStage<List<Account>> getPageIndividuals(String organization, Integer offset, Integer length);

    CompletionStage<List<Account>> getAllProfessionals(String organization);

    CompletionStage<List<Account>> getPageProfessionals(String organization, Integer offset, Integer length);

    CompletionStage<List<Account>> getAdministrativeValidatorsForExport(String organization);

    CompletionStage<List<Account>> suggest(String organization, String pattern);

    CompletionStage<List<Account>> search(String organization, String pattern);

    CompletionStage<List<Account>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Boolean> delete(String organization, String uuid);

    CompletionStage<api.v1.models.Account> serialize(String organization, Account account);

    CompletionStage<Boolean> changeStatusWhenAddingEstablishment(String organization, String establishment);

    CompletionStage<Boolean> reindex(String organization);

    CompletionStage<PaginatedResult<List<IndexableProfessional>>> getProfessionalOverviews(final String organization, final Pageable pageable);

    CompletionStage<PaginatedResult<List<IndexableIndividual>>> getIndividualOverviews(final String organization, final Pageable pageable);

    CompletionStage<PaginatedResult<List<IndexableAccount>>> getAccountOverviews(final String organization, final Pageable pageable);

    CompletionStage<List<AccountComment>> getComments(final String organization, final String uuid);

    CompletionStage<Optional<AccountComment>> addComment(final String organization, AccountComment accountComment);

    CompletionStage<Optional<api.v1.models.AccountComment>> serializeComment(String organization, AccountComment comment);

}
