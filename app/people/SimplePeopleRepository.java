package people;

import addresses.AddressWithRole;
import addresses.AddressesRepository;
import core.BaseRepository;
import establishments.EstablishmentDelegateRole;
import establishments.EstablishmentPeopleRole;
import markets.MarketEstablishmentRole;
import markets.MarketPeopleRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;
import scala.Tuple2;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SimplePeopleRepository extends BaseRepository<People, String> implements PeopleRepository {

    protected static final Logger logger = LoggerFactory.getLogger(SimplePeopleRepository.class);

    protected final Database database;
    protected final AddressesRepository addressesRepository;

    @Inject
    SimplePeopleRepository(@NamedDatabase("crm") Database db, AddressesRepository addressesRepository) {
        super(db, logger);
        this.database = db;
        this.addressesRepository = addressesRepository;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected People parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String title = result.getString("title");
        final String lastName = result.getString("lastname");
        final String firstName = result.getString("firstname");

        final Optional<String> workMail = Optional.ofNullable(result.getString("workmail"));
        final Optional<String> email = Optional.ofNullable(result.getString("email"));
        final Optional<String> workPhone = Optional.ofNullable(result.getString("workphone"));
        final Optional<String> mobilePhone = Optional.ofNullable(result.getString("mobilephone"));
        final Optional<String> jobDescription = Optional.ofNullable(result.getString("jobdescription"));

        return new People(Optional.of(uuid), title, lastName, firstName, workMail, email, workPhone, mobilePhone, jobDescription);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people\" WHERE LOWER(firstname || ' ' ||  lastname)::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT 100");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people\" WHERE LOWER(firstname || ' ' ||  lastname)::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final People people) throws SQLException {

        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"people\"(uuid, title, lastname, firstname, workmail, email, workphone, mobilephone, jobdescription, tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, people.uuid);
        ps.setString(2, people.title);
        ps.setString(3, people.lastname);
        ps.setString(4, people.firstname);
        ps.setString(5, people.workMail.orElse(null));
        ps.setString(6, people.email.orElse(null));
        ps.setString(7, people.workPhone.orElse(null));
        ps.setString(8, people.mobilePhone.orElse(null));
        ps.setString(9, people.jobDescription.orElse(null));
        ps.setString(10, organization);

        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"people\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final People people) throws SQLException {

        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"people\" SET title = ?, lastname = ?, firstname = ?, workmail = ?, email = ?, workphone = ?, mobilephone = ?, jobdescription = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, people.title);
        ps.setString(2, people.lastname);
        ps.setString(3, people.firstname);
        ps.setString(4, people.workMail.orElse(null));
        ps.setString(5, people.email.orElse(null));
        ps.setString(6, people.workPhone.orElse(null));
        ps.setString(7, people.mobilePhone.orElse(null));
        ps.setString(8, people.jobDescription.orElse(null));
        ps.setString(9, people.uuid);
        ps.setString(10, organization);

        return ps;
    }

    @Override
    public List<Tuple2<People, String>> getPurchasers(String organization, String establishment, Optional<String> market) {
        return database.withConnection(tr -> {
            try {
                List<Tuple2<People, String>> peopleWithOrigin = new ArrayList<>();
                Set<String> keys = new HashSet<>();
                final PreparedStatement ps = tr.prepareStatement("SELECT people.*, establishments.name as establishmentname FROM \"establishments\",\"establishment_people\",\"people\" WHERE people.uuid = people_id AND establishment_id = establishments.uuid AND establishment_id = ? AND role = ? AND establishments.tenant=?");
                ps.setString(1, establishment);
                ps.setString(2, EstablishmentPeopleRole.PURCHASER.toString());
                ps.setString(3, organization);
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    People people = parseOneItem(result, organization);
                    if (!keys.contains(people.uuid)) {
                        keys.add(people.uuid);
                        String origin = "Établissement " + result.getString("establishmentname");
                        peopleWithOrigin.add(new Tuple2<>(people, origin));
                    }
                }
                final PreparedStatement ps2 = tr.prepareStatement("SELECT people.*, establishments.name as establishmentname FROM \"establishments\",\"establishment_delegates\",\"establishment_people\",\"people\" WHERE people.uuid = people_id AND establishment_delegates.delegate_id = establishments.uuid AND establishment_people.establishment_id = delegate_id AND establishment_delegates.establishment_id = ? AND establishment_delegates.role = ? AND establishment_people.role = ? AND establishments.tenant=?");
                ps2.setString(1, establishment);
                ps2.setString(2, EstablishmentDelegateRole.PURCHASER.toString());
                ps2.setString(3, EstablishmentPeopleRole.MAIN.toString());
                ps2.setString(4, organization);

                final ResultSet result2 = ps2.executeQuery();
                while (result2.next()) {
                    People people = parseOneItem(result2, organization);
                    if (!keys.contains(people.uuid)) {
                        keys.add(people.uuid);
                        String origin = "Établissement " + result2.getString("establishmentname");
                        peopleWithOrigin.add(new Tuple2<>(parseOneItem(result2, organization), origin));
                    }
                }
                if (market.isPresent()) {
                    final PreparedStatement ps3 = tr.prepareStatement("SELECT people.*, markets.name as marketname FROM \"markets\",\"markets_people\",\"people\" WHERE people.uuid = people_id AND markets_id = markets.uuid AND markets_id = ? AND role = ? AND markets.tenant=?");
                    ps3.setString(1, market.get());
                    ps3.setString(2, MarketPeopleRole.PURCHASER.toString());
                    ps3.setString(3, organization);
                    final ResultSet result3 = ps3.executeQuery();
                    while (result3.next()) {
                        People people = parseOneItem(result3, organization);
                        if (!keys.contains(people.uuid)) {
                            keys.add(people.uuid);
                            String origin = "Marché " + result3.getString("marketname");
                            peopleWithOrigin.add(new Tuple2<>(people, origin));
                        }
                    }

                    final PreparedStatement ps4 = tr.prepareStatement("SELECT people.*, establishments.name as establishmentname FROM \"establishments\",\"markets_establishments\",\"establishment_people\",\"people\" WHERE people.uuid = people_id AND markets_establishments.establishment_id = establishments.uuid AND establishment_people.establishment_id = markets_establishments.establishment_id AND markets_id = ? AND markets_establishments.role = ? AND establishment_people.role = ? AND establishments.tenant=?");
                    ps4.setString(1, market.get());
                    ps4.setString(2, MarketEstablishmentRole.PURCHASER.toString());
                    ps4.setString(3, EstablishmentPeopleRole.MAIN.toString());
                    ps4.setString(4, organization);
                    final ResultSet result4 = ps.executeQuery();
                    while (result4.next()) {
                        People people = parseOneItem(result4, organization);
                        if (!keys.contains(people.uuid)) {
                            keys.add(people.uuid);
                            String origin = "Établissement " + result4.getString("establishmentname");
                            peopleWithOrigin.add(new Tuple2<>(people, origin));
                        }
                    }
                }
                return peopleWithOrigin;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get purchasers for establishment " + establishment + " from database", e);
            }
        });
    }

    @Override
    public List<AddressWithRole> getAddressesByRole(String organization, String uuid, String role) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people_addresses\" WHERE people_id = ? AND role = ?");
                ps.setString(1, uuid);
                ps.setString(2, role);
                final ResultSet result = ps.executeQuery();
                final List<AddressWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new AddressWithRole(result.getString("address_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    public List<AddressWithRole> getAddresses(String organization, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"people_addresses\" WHERE people_id = ?");
                ps.setString(1, uuid);
                final ResultSet result = ps.executeQuery();
                final List<AddressWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new AddressWithRole(result.getString("address_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    public Boolean addAddress(final String peopleId, final String addressId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("INSERT INTO \"people_addresses\"(people_id, address_id, role) VALUES (?, ?, ?)");
                ps.setString(1, peopleId);
                ps.setString(2, addressId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link address to people from database", e);
            }
        });
    }

    @Override
    public Boolean removeAddress(final String peopleId, final String addressId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("DELETE FROM \"people_addresses\" WHERE people_id = ? AND address_id = ? AND role = ?");
                ps.setString(1, peopleId);
                ps.setString(2, addressId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove link between address and people from database", e);
            }
        });
    }
}
