package addresses;

import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.util.Optional;

public class SimpleAddressesRepository extends BaseRepository<Address, String> implements AddressesRepository {

    protected static final Logger log = LoggerFactory.getLogger(SimpleAddressesRepository.class);

    protected final Database database;

    @Inject
    SimpleAddressesRepository(@NamedDatabase("crm") Database db) {
        super(db, log);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"addresses\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"addresses\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Address parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
        final Optional<String> type = Optional.ofNullable(result.getString("type"));
        final Optional<String> address1 = Optional.ofNullable(result.getString("address1"));
        final Optional<String> address2 = Optional.ofNullable(result.getString("address2"));
        final Optional<String> postCode = Optional.ofNullable(result.getString("post_code"));
        final Optional<String> city = Optional.ofNullable(result.getString("city"));
        final Optional<String> gpsCoordinates = Optional.ofNullable(result.getString("gps_coordinates"));
        final Optional<String> inseeCoordinates = Optional.ofNullable(result.getString("insee_coordinates"));
        final Optional<String> dispatch = Optional.ofNullable(result.getString("dispatch"));
        final Optional<String> staircase = Optional.ofNullable(result.getString("staircase"));
        final Optional<String> wayType = Optional.ofNullable(result.getString("way_type"));
        final Optional<String> country = Optional.ofNullable(result.getString("country"));
        final Optional<Timestamp> created = Optional.ofNullable(result.getTimestamp("created"));
        final Optional<java.util.Date> createdDate = created.map(timestamp -> new java.util.Date(timestamp.getTime()));
        return new Address(Optional.of(uuid.get()), type.get(), address1, address2, postCode, city, gpsCoordinates,
                inseeCoordinates, dispatch, staircase, wayType, country, createdDate.get());
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"addresses\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"addresses\" WHERE LOWER(\"address1\")::tsvector  @@ LOWER(?)::tsquery AND tenant=?");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"addresses\" WHERE LOWER(\"address1\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Address address) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"addresses\"(uuid, \"type\", address1, address2, post_code, city, gps_coordinates, insee_coordinates, dispatch, staircase, way_type, country, created, tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, address.uuid);
        ps.setString(2, address.type);
        ps.setString(3, address.address1.orElse(null));
        ps.setString(4, address.address2.orElse(null));
        ps.setString(5, address.postCode.orElse(null));
        ps.setString(6, address.city.orElse(null));
        ps.setString(7, address.gpsCoordinates.orElse(null));
        ps.setString(8, address.inseeCoordinates.orElse(null));
        ps.setString(9, address.dispatch.orElse(null));
        ps.setString(10, address.staircase.orElse(null));
        ps.setString(11, address.wayType.orElse(null));
        ps.setString(12, address.country.orElse(null));
        ps.setTimestamp(13, new Timestamp(address.created.getTime()));
        ps.setString(14, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"addresses\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Address address) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"addresses\" SET \"type\" = ?, address1 = ?, address2 = ?, post_code = ?, city = ?, gps_coordinates = ?, insee_coordinates = ?, dispatch = ?, staircase = ?, way_type = ?, country = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, address.type);
        ps.setString(2, address.address1.orElse(null));
        ps.setString(3, address.address2.orElse(null));
        ps.setString(4, address.postCode.orElse(null));
        ps.setString(5, address.city.orElse(null));
        ps.setString(6, address.gpsCoordinates.orElse(null));
        ps.setString(7, address.inseeCoordinates.orElse(null));
        ps.setString(8, address.dispatch.orElse(null));
        ps.setString(9, address.staircase.orElse(null));
        ps.setString(10, address.wayType.orElse(null));
        ps.setString(11, address.country.orElse(null));
        ps.setString(12, address.uuid);
        ps.setString(13, organization);
        return ps;
    }

}
