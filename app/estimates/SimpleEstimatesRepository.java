package estimates;

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
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SimpleEstimatesRepository extends BaseRepository<Estimate, String> implements EstimatesRepository {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleEstimatesRepository.class);

    protected final Database database;

    @Inject
    SimpleEstimatesRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Estimate parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final Optional<String> market = Optional.ofNullable(result.getString("market"));
        final Optional<String> account = Optional.ofNullable(result.getString("account"));
        return new Estimate(Optional.of(uuid), name, market, account);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT 100");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Estimate estimate) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"estimates\"(uuid, name, market, account, tenant) values(?, ?, ?, ?, ?)");
        ps.setString(1, estimate.uuid);
        ps.setString(2, estimate.name);
        ps.setString(3, estimate.market.orElse(null));
        ps.setString(4, estimate.account.orElse(null));
        ps.setString(5, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"estimates\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Estimate item) throws SQLException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public List<Estimate> getFromMarket(String organization, String marketUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE market = ? AND tenant=?");
                ps.setString(1, marketUuid);
                ps.setString(2, organization);
                final List<Estimate> estimatesList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    estimatesList.add(parseOneItem(result, organization));
                }
                return estimatesList;
            } catch (SQLException e) {
                logger.error("Failed to get estimates from market " + marketUuid + ".", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Estimate> getFromAccount(String organization, String accountUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"estimates\" WHERE account = ? AND tenant=?");
                ps.setString(1, accountUuid);
                ps.setString(2, organization);
                final List<Estimate> estimatesList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    estimatesList.add(parseOneItem(result, organization));
                }
                return estimatesList;
            } catch (SQLException e) {
                logger.error("Failed to get estimates from account " + accountUuid + ".", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Boolean patch(String organization, String uuid, String name, Optional<String> market, Optional<String> account) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"estimates\" SET name = ?, market = ?, account = ? WHERE uuid = ? AND tenant=?");
                ps.setString(1, name);
                ps.setString(2, market.orElse(null));
                ps.setString(3, account.orElse(null));
                ps.setString(4, uuid);
                ps.setString(5, organization);
                return ps.execute();
            } catch (SQLException e) {
                logger.error("Failed to update estimate " + uuid + ".", e);
                return false;
            }
        });

    }

}
