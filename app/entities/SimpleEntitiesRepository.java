package entities;

import addresses.AddressesRepository;
import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.util.Date;
import java.util.Optional;

public class SimpleEntitiesRepository extends BaseRepository<Entity, String> implements EntitiesRepository {

    protected static final Logger log = LoggerFactory.getLogger(SimpleEntitiesRepository.class);

    protected final Database database;
    protected final AddressesRepository addressesRepository;

    @Inject
    SimpleEntitiesRepository(@NamedDatabase("crm") Database db, AddressesRepository addressesRepository) {
        super(db, log);
        this.database = db;
        this.addressesRepository = addressesRepository;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE tenant=? ORDER BY \"name\" LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Entity parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final String corporateName = result.getString("corporate_name");
        final String siren = result.getString("siren");

        final Optional<String> type = Optional.ofNullable(result.getString("type"));
        final Optional<String> domain = Optional.ofNullable(result.getString("domain"));
        final Optional<String> logo = Optional.ofNullable(result.getString("logo"));
        final Optional<String> description = Optional.ofNullable(result.getString("description"));
        final Optional<String> optionalMainAddress = Optional.ofNullable(result.getString("main_address"));

        final Date createdDate = new Date(result.getTimestamp("created").getTime());

        return new Entity(Optional.of(uuid), name, corporateName, type, siren, domain, logo, description, optionalMainAddress, createdDate);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    public Optional<Entity> getFromSiren(final String organization, final String siren) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE tenant = ? AND siren = ?");
                ps.setString(1, organization);
                ps.setString(2, siren);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                return Optional.empty();
            }
        });
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"entities\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Entity entity) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"entities\"(uuid, \"name\", corporate_name, \"type\", siren, \"domain\", logo, description, main_address, created, tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, entity.uuid);
        ps.setString(2, entity.name);
        ps.setString(3, entity.corporateName);
        ps.setString(4, entity.type.orElse(null));
        ps.setString(5, entity.siren);
        ps.setString(6, entity.domain.orElse(null));
        ps.setString(7, entity.logo.orElse(null));
        ps.setString(8, entity.description.orElse(null));
        ps.setString(9, entity.mainAddress.orElse(null));
        ps.setTimestamp(10, new Timestamp(entity.created.getTime()));
        ps.setString(11, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"entities\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Entity entity) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"entities\" SET \"name\" = ?, corporate_name = ?, \"type\" = ?, siren = ?, \"domain\" = ?, logo = ?, description = ?, main_address = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, entity.name);
        ps.setString(2, entity.corporateName);
        ps.setString(3, entity.type.orElse(null));
        ps.setString(4, entity.siren);
        ps.setString(5, entity.domain.orElse(null));
        ps.setString(6, entity.logo.orElse(null));
        ps.setString(7, entity.description.orElse(null));
        ps.setString(8, entity.mainAddress.orElse(null));
        ps.setString(9, entity.uuid);
        ps.setString(10, organization);
        return ps;
    }

    @Override
    public Optional<AdnParameters> getAdnParameters(String organization, String adnName, Optional<String> address1, Optional<String> address2, Optional<String> zip, Optional<String> city) {
        return database.withConnection(transaction -> {
            try {
                String query = "SELECT * FROM \"adnparameters\" WHERE name=? AND tenant=?";
                if (address1.isPresent()) {
                    query = query + " AND address1=?";
                }
                if (address2.isPresent()) {
                    query = query + " AND address2=?";
                }
                if (zip.isPresent()) {
                    query = query + " AND zip=?";
                }
                if (city.isPresent()) {
                    query = query + " AND city=?";
                }
                final PreparedStatement ps = transaction.prepareStatement(query);
                ps.setString(1, adnName);
                ps.setString(2, organization);
                Integer count = 3;
                if (address1.isPresent()) {
                    ps.setString(count, address1.get());
                    count = count + 1;
                }
                if (address2.isPresent()) {
                    ps.setString(count, address2.get());
                    count = count + 1;
                }
                if (zip.isPresent()) {
                    ps.setString(count, zip.get());
                    count = count + 1;
                }
                if (city.isPresent()) {
                    ps.setString(count, city.get());
                }
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    final Optional<String> account = Optional.ofNullable(result.getString("entity"));
                    final Integer adnId = result.getInt("adnid");
                    return Optional.of(new AdnParameters(account.get(), adnId));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                log.error("Failed to get cliententity " + adnName + " from database", e);
                return Optional.empty();
            }
        });
    }
}
