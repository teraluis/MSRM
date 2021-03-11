package activities;

import core.BaseRepository;
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
import java.util.Date;
import java.util.Optional;

public class SimpleActivitiesRepository extends BaseRepository<Activity, String> implements ActivitiesRepository {

    protected static final Logger log = LoggerFactory.getLogger(SimpleActivitiesRepository.class);

    protected final Database database;

    @Inject
    SimpleActivitiesRepository(@NamedDatabase("crm") Database db) {
        super(db, log);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"activities\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"activities\" WHERE tenant=? ORDER BY \"name\" LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Activity parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final Optional<String> description = Optional.ofNullable(result.getString("description"));
        final Date createdDate = new java.util.Date(result.getTimestamp("created").getTime());

        return new Activity(Optional.of(uuid), name, description, createdDate);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"activities\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"activities\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"activities\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Activity activity) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"activities\"(uuid, \"name\", description, tenant, created) values(?, ?, ?, ?, ?)");
        ps.setString(1, activity.uuid);
        ps.setString(2, activity.name);
        ps.setString(3, activity.description.orElse(null));
        ps.setString(4, organization);
        ps.setTimestamp(5, new Timestamp(activity.created.getTime()));
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"activities\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Activity activity) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"activities\" SET \"name\" = ?, description = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, activity.name);
        ps.setString(2, activity.description.orElse(null));
        ps.setString(3, activity.uuid);
        ps.setString(4, organization);
        return ps;
    }
}
