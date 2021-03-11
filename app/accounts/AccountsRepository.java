package accounts;

import java.util.List;
import java.util.Optional;

public interface AccountsRepository {

    Optional<String> add(String organization, Account account);

    Optional<Account> update(String organization, Account account);

    Boolean linkAccountGroup(String accountId, String groupId);

    Boolean unlinkAccountGroup(String accountId, String groupId);

    Optional<Account> get(String organization, String uuid);

    Optional<Account> getFromEntity(String organization, String entityId);

    List<Account> getAll(String organization);

    List<Account> getAllNotDeleted(String organization);

    List<Account> getPage(String organization, Integer offset, Integer length);

    List<Account> getAllIndividuals(String organization);

    List<Account> getPageIndividuals(String organization, Integer offset, Integer length);

    List<Account> getAllProfessionals(String organization);

    List<Account> getPageProfessionals(String organization, Integer offset, Integer length);

    List<Account> getAdministrativeValidatorsForExport(String organization);

    List<Account> suggest(String organization, String pattern);

    List<Account> search(String organization, String pattern);

    List<Account> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<String> delete(String organization, String uuid);

    List<AccountComment> getComments(String organization, String uuid);

    Optional<AccountComment> addComment(String organization, AccountComment accountComment);

    Boolean changeStatusWhenAddingEstablishment(String organization, String establishment);

    boolean deleteOne(String uuid);
}
