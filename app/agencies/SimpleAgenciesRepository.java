package agencies;

import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;
import users.UsersRepository;

import javax.inject.Inject;
import java.sql.*;
import java.util.Optional;

public class SimpleAgenciesRepository extends BaseRepository<Agency, String> implements AgenciesRepository {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleAgenciesRepository.class);

    protected final Database database;
    protected final UsersRepository usersRepository;

    @Inject
    SimpleAgenciesRepository(@NamedDatabase("crm") Database db, UsersRepository usersRepository) {
        super(db, logger);
        this.database = db;
        this.usersRepository = usersRepository;
    }


    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"agencies\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"agencies\" WHERE tenant=? ORDER BY \"name\" LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Agency parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
        final Optional<String> code = Optional.ofNullable(result.getString("code"));
        final Optional<String> name = Optional.ofNullable(result.getString("name"));
        final Optional<String> optionalManager = Optional.ofNullable(result.getString("manager"));
        final Optional<String> referenceIban = Optional.ofNullable(result.getString("reference_iban"));
        final Optional<String> referenceBic = Optional.ofNullable(result.getString("reference_bic"));
        final Optional<Timestamp> created = Optional.ofNullable(result.getTimestamp("created"));
        final Optional<java.util.Date> createdDate = created.map(timestamp -> new java.util.Date(timestamp.getTime()));
        return new Agency(Optional.of(uuid.get()), code.orElse(""), name.get(), optionalManager.get(), createdDate.get(), referenceIban.get(), referenceBic.get());
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"agencies\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"agencies\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"agencies\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Agency agency) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"agencies\"(uuid, \"name\", manager, tenant, created, code, reference_iban, reference_bic) values(?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, agency.uuid);
        ps.setString(2, agency.name);
        ps.setString(3, agency.manager);
        ps.setString(4, organization);
        ps.setTimestamp(5, new Timestamp(agency.created.getTime()));
        ps.setString(6, agency.code);
        ps.setString(7, agency.referenceIban);
        ps.setString(8, agency.referenceBic);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"agencies\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Agency agency) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"agencies\" SET \"name\" = ?, manager = ?, code = ?, reference_iban = ?, reference_bic = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, agency.name);
        ps.setString(2, agency.manager);
        ps.setString(3, agency.code);
        ps.setString(4, agency.referenceIban);
        ps.setString(5, agency.referenceBic);
        ps.setString(6, agency.uuid);
        ps.setString(7, organization);
        return ps;
    }

    @Override
    public Optional<Agency> getFromOfficeName(String organization, String officeName) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("SELECT agencies.* FROM \"office\",\"agencies\" WHERE office.agency = agencies.uuid AND office.name = ?");
                ps.setString(1, officeName);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get agency from office name " + officeName + ".", e);
                return Optional.empty();
            }
        });
    }
}
