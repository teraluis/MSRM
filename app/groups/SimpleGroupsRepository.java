package groups;

import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SimpleGroupsRepository extends BaseRepository<Group, String> implements GroupsRepository {

    protected final Database database;
    protected static final Logger log = LoggerFactory.getLogger(SimpleGroupsRepository.class);

    @Inject
    SimpleGroupsRepository(@NamedDatabase("crm") Database db) {
        super(db, log);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE tenant=? ORDER BY \"name\" LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Group parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final String type = result.getString("type");
        final Optional<String> category = Optional.ofNullable(result.getString("category"));
        final Optional<String> iban = Optional.ofNullable(result.getString("iban"));
        final Optional<String> description = Optional.ofNullable(result.getString("description"));
        final java.util.Date createdDate = new java.util.Date(result.getTimestamp("created").getTime());

        return new Group(Optional.of(uuid), name, type, category, iban, description, createdDate);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Group group) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"accountgroups\"(uuid, \"name\", \"type\", category, iban, description, tenant, created) values(?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, group.uuid);
        ps.setString(2, group.name);
        ps.setString(3, group.type);
        ps.setString(4, group.category.orElse(null));
        ps.setString(5, group.iban.orElse(null));
        ps.setString(6, group.description.orElse(null));
        ps.setString(7, organization);
        ps.setTimestamp(8, new Timestamp(group.created.getTime()));
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"accountgroups\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Group group) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"accountgroups\" SET \"name\" = ?, \"type\" = ?, category = ?, iban = ?, description = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, group.name);
        ps.setString(2, group.type);
        ps.setString(3, group.category.orElse(null));
        ps.setString(4, group.iban.orElse(null));
        ps.setString(5, group.description.orElse(null));
        ps.setString(6, group.uuid);
        ps.setString(7, organization);
        return ps;
    }

    @Override
    public List<Group> getFromAccount(final String organization, final String accountId) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"accountgroups\" WHERE uuid IN (SELECT accountaccountgroups.group FROM \"accountaccountgroups\" WHERE account = ?) AND tenant = ?");
                ps.setString(1, accountId);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                final List<Group> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneItem(result, organization));
                }
                return items;
            } catch (SQLException e) {
                log.error("Failed to get item from database", e);
                return new ArrayList<>();
            }
        });
    }
}
