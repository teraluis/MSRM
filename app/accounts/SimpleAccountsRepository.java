package accounts;

import core.BaseRepository;
import establishments.EstablishmentDelegateRole;
import core.EventType;
import markets.MarketEstablishmentRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

public class SimpleAccountsRepository extends BaseRepository<Account, String> implements AccountsRepository {

    protected final Database database;
    protected static final Logger log = LoggerFactory.getLogger(SimpleAccountsRepository.class);

    @Inject
    SimpleAccountsRepository(@NamedDatabase("crm") Database db) {
        super(db, log);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    public List<Account> getAllIndividuals(final String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE entity IS NULL AND tenant=?");
                ps.setString(1, organization);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Account> getPageIndividuals(final String organization, final Integer offset, final Integer length) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE entity IS NULL AND tenant=? LIMIT ? OFFSET ?");
                ps.setString(1, organization);
                ps.setInt(2, length);
                ps.setInt(3, offset);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Account> getAllProfessionals(final String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE entity IS NOT NULL AND tenant=?");
                ps.setString(1, organization);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Account> getPageProfessionals(final String organization, final Integer offset, final Integer length) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE entity IS NOT NULL AND tenant=? LIMIT ? OFFSET ?");
                ps.setString(1, organization);
                ps.setInt(2, length);
                ps.setInt(3, offset);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Account> getAdministrativeValidatorsForExport(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\",\"markets_accounts\" WHERE accounts_id = uuid AND role = ? AND tenant=?");
                ps.setString(1, MarketEstablishmentRole.ADMINISTRATIVE_VALIDATOR.toString());
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                final PreparedStatement ps2 = transaction.prepareStatement("SELECT * FROM \"accounts\",\"establishment_accounts\" WHERE account_id = uuid AND role = ? AND tenant=?");
                ps2.setString(1, EstablishmentDelegateRole.ADMINISTRATIVE.toString());
                ps2.setString(2, organization);
                final ResultSet result2 = ps2.executeQuery();
                while (result2.next()) {
                    items.add(parseOneItem(result2, organization));
                }
                return items;
            } catch (SQLException e) {
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Account parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String type = result.getString("type");
        final String reference = result.getString("reference");
        final String category = result.getString("category");
        final String commercial = result.getString("commercial");
        final String contact = result.getString("contact");

        final Optional<String> importance = Optional.ofNullable(result.getString("importance"));
        final Optional<String> state = Optional.ofNullable(result.getString("state"));
        final Optional<String> entity = Optional.ofNullable(result.getString("entity"));
        final Optional<Integer> maxPaymentTime = Optional.of(result.getInt("max_payment_time"));
        final Optional<String> legacyCode = Optional.ofNullable(result.getString("legacy_code"));

        final Date createdDate = new Date(result.getTimestamp("created").getTime());

        return new Account(Optional.of(uuid), type, reference, category, commercial, contact,
                importance, state, entity, maxPaymentTime, legacyCode, createdDate);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    public Optional<Account> getFromEntity(final String organization, final String entityId) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE tenant=? AND entity=?");
                ps.setString(1, organization);
                ps.setString(2, entityId);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                log.error("Failed to get item from database", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Account> suggest(final String organization, final String pattern) {
        final List<String> tokens = Arrays.asList(pattern.trim().split(" "));
        final StringJoiner joiner = new StringJoiner(":* & ", "", ":*");
        tokens.forEach(token -> joiner.add(token));
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE entity IN (SELECT uuid FROM \"entities\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?)");
                ps.setString(1, joiner.toString());
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                final List<Account> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE LOWER(\"reference\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts\" WHERE LOWER(\"reference\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Account account) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"accounts\"(uuid, \"type\", reference, category, commercial, contact, importance, state, entity, max_payment_time, created, tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, account.uuid);
        ps.setString(2, account.type);
        ps.setString(3, account.reference);
        ps.setString(4, account.category);
        ps.setString(5, account.commercial);
        ps.setString(6, account.contact);
        ps.setString(7, account.importance.orElse(null));
        ps.setString(8, account.state.orElse(null));
        ps.setString(9, account.entity.orElse(null));
        ps.setInt(10, account.maxPaymentTime.orElse(0));
        ps.setTimestamp(11, new Timestamp(account.created.getTime()));
        ps.setString(12, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"accounts\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Account account) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"accounts\" SET \"type\" = ?, reference = ?, category = ?, commercial = ?, contact = ?, importance = ?, state = ?, entity = ?, max_payment_time = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, account.type);
        ps.setString(2, account.reference);
        ps.setString(3, account.category);
        ps.setString(4, account.commercial);
        ps.setString(5, account.contact);
        ps.setString(6, account.importance.orElse(null));
        ps.setString(7, account.state.orElse(null));
        ps.setString(8, account.entity.orElse(null));
        ps.setInt(9, account.maxPaymentTime.orElse(0));
        ps.setString(10, account.uuid);
        ps.setString(11, organization);
        return ps;
    }

    @Override
    public Boolean linkAccountGroup(final String accountId, final String groupId) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("INSERT INTO \"accountaccountgroups\"(account, \"group\") VALUES (?, ?)");
                ps.setString(1, accountId);
                ps.setString(2, groupId);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                log.error(String.format("Could link account %s with group %s", accountId, groupId), e);
                return false;
            }
        });
    }

    @Override
    public Boolean unlinkAccountGroup(String accountId, String groupId) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("DELETE FROM \"accountaccountgroups\" WHERE account = ? AND \"group\" = ?");
                ps.setString(1, accountId);
                ps.setString(2, groupId);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                log.error("Could not list all account groups from database", e);
                return false;
            }
        });
    }

    @Override
    public List<AccountComment> getComments(String organization, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accounts_comments\" WHERE id_account=?");
                ps.setString(1, uuid);
                final List<AccountComment> commentList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    commentList.add(new AccountComment(Optional.of(result.getString("uuid")),
                            result.getString("id_account"),
                            Optional.ofNullable(result.getString("id_user")),
                            result.getString("comment"),
                            new java.util.Date(result.getTimestamp("created").getTime()),
                            EventType.valueOf(result.getString("event_type"))));
                }
                return commentList;
            } catch (SQLException e) {
                log.error("Failed to get comments from account " + uuid, e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Optional<AccountComment> addComment(String organization, AccountComment comment) {
        return database.withConnection(transaction -> {
            try {
                final String sql = "INSERT INTO \"accounts_comments\" (uuid, id_account, id_user, comment, created, event_type) VALUES (?, ?, ?, ?, ?, ?)";
                final PreparedStatement ps = transaction.prepareStatement(sql);
                ps.setString(1, comment.uuid);
                ps.setString(2, comment.idAccount);
                ps.setString(3, comment.idUser.orElse(null));
                ps.setString(4, comment.comment);
                ps.setTimestamp(5, new Timestamp(comment.created.getTime()));
                ps.setString(6, comment.event.toString());
                ps.execute();
                return Optional.of(comment);
            } catch (SQLException e) {
                log.error("Failed to add comment to comment " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    @Override
    public Boolean changeStatusWhenAddingEstablishment(String organization, String establishment) {
        return database.withConnection(transaction -> {
            try {
                final String accountUuidRequest = "SELECT accounts.uuid as accountuuid FROM accounts, entities, establishments WHERE accounts.entity = entities.uuid AND establishments.entity = entities.uuid AND entities.tenant = accounts.tenant AND establishments.uuid = ? AND entities.tenant = ?";
                final PreparedStatement ps = transaction.prepareStatement(accountUuidRequest);
                ps.setString(1, establishment);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    String accountUuid = result.getString("accountuuid");
                    final String stateUpdate = "UPDATE accounts SET state = ? WHERE accounts.uuid = ? AND tenant = ?";
                    final PreparedStatement ps2 = transaction.prepareStatement(stateUpdate);
                    ps2.setString(1, "1");
                    ps2.setString(2, accountUuid);
                    ps2.setString(3, organization);
                    ps2.execute();
                    return true;
                } else {
                    return false;
                }
            } catch (SQLException e) {
                log.error("Failed to change account status when adding establishement " + e.getMessage());
                return false;
            }
        });
    }

    public boolean deleteOne(String accountId) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT count(1) as total FROM accountaccountgroups where account = ?");
                ps.setString(1, accountId);
                ResultSet result = ps.executeQuery();
                boolean noLink = !result.next() || result.getInt(1) == 0;

                ps = transaction.prepareStatement("SELECT count(1) as total FROM accounts_comments where id_account = ?");
                ps.setString(1, accountId);
                result = ps.executeQuery();
                noLink = result.next() ? noLink && result.getInt(1) == 0 : noLink;

                ps = transaction.prepareStatement("SELECT count(1) as total FROM establishment_accounts where account_id = ?");
                ps.setString(1, accountId);
                result = ps.executeQuery();
                noLink = result.next() ? noLink && result.getInt(1) == 0 : noLink;

                ps = transaction.prepareStatement("SELECT count(1) as total FROM orders where purchaser = ?");
                ps.setString(1, accountId);
                result = ps.executeQuery();
                noLink = result.next() ? noLink && result.getInt(1) == 0 : noLink;

                ps = transaction.prepareStatement("SELECT count(1) as total FROM estimates where account = ?");
                ps.setString(1, accountId);
                result = ps.executeQuery();
                noLink = result.next() ? noLink && result.getInt(1) == 0 : noLink;

                if (noLink) {
                    ps = transaction.prepareStatement("DELETE FROM accounts where uuid = ?");
                } else {
                    ps = transaction.prepareStatement("UPDATE accounts set deleted = true where uuid = ?");
                }
                ps.setString(1, accountId);
                ps.execute();
                return true;

            } catch (SQLException e) {
                log.error("Failed to delete or disable account ", e);
                return false;
            }
        });
    }

    public List<Account> getAllNotDeleted(String orga) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM accounts where tenant = ? and (deleted = false or deleted is null)");
                ps.setString(1, orga);
                ResultSet result = ps.executeQuery();

                final List<Account> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, orga));
                }

                return items;

            } catch (SQLException e) {
                log.error("Failed to delete or disable account ", e);
                return new ArrayList<>();
            }
        });
    }
}
