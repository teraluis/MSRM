package establishments;

import addresses.AddressWithRole;
import core.BaseRepository;
import core.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import people.PeopleWithRole;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SimpleEstablishmentsRepository extends BaseRepository<Establishment, String> implements EstablishmentsRepository {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleEstablishmentsRepository.class);

    protected final Database database;

    private String generateSageCode() {
        int min = 0;
        int max = 9999999;
        int random_int = (int) (Math.random() * (max - min + 1) + min);
        return String.format("%07d", random_int);
    }

    @Inject
    SimpleEstablishmentsRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    @Override
    public List<Establishment> getFromEntity(final String organization, final String entity) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE tenant=? AND entity=? AND deleted = false");
                ps.setString(1, organization);
                ps.setString(2, entity);
                final ResultSet result = ps.executeQuery();
                final List<Establishment> items = new ArrayList<>();
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
    public String getSageCode(String organization) {
        Optional<String> sageCode = Optional.empty();
        while (!sageCode.isPresent()) {
            String randomSageCode = generateSageCode();
            Boolean isPresent = database.withConnection(transaction -> {
                try {
                    final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE sage_code=?");
                    ps.setString(1, randomSageCode);
                    final ResultSet result = ps.executeQuery();
                    return result.next();
                } catch (SQLException e) {
                    logger.error("Error when getting establishement from sage code.");
                    return true;
                }
            });
            if (!isPresent) {
                sageCode = Optional.of(randomSageCode);
            }
        }
        return sageCode.get();
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE tenant=? AND deleted = false");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE tenant=? AND deleted = false LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? AND deleted = false");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? AND deleted = false LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);

        return ps;
    }

    @Override
    protected Establishment parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final String corporateName = result.getString("corporate_name");
        final String siret = result.getString("siret");
        final String sageCode = result.getString("sage_code");
        final String iban = result.getString("iban");
        final String bic = result.getString("bic");
        final String facturationAnalysis = result.getString("facturation_analysis");
        final String agency = result.getString("agency_id");
        final String entity = result.getString("entity");
        final Date createdDate = new Date(result.getTimestamp("created").getTime());

        final Optional<String> description = Optional.ofNullable(result.getString("description"));
        final Optional<String> mail = Optional.ofNullable(result.getString("mail"));
        final Optional<String> phone = Optional.ofNullable(result.getString("phone"));
        final Optional<String> activity = Optional.ofNullable(result.getString("activity"));

        final Boolean clientExported = result.getBoolean("client_exported");
        final Boolean clientModified = result.getBoolean("client_modified");
        final Boolean validatorExported = result.getBoolean("validator_exported");
        final Boolean validatorModified = result.getBoolean("validator_modified");

        // Build establishment
        return new Establishment(Optional.of(uuid), name, corporateName, siret, sageCode, description, mail, phone, activity, entity, createdDate, clientExported, clientModified, validatorExported, validatorModified, iban, bic, facturationAnalysis, agency);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    public Optional<Establishment> getFromSiret(final String organization, final String siret) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishments\" WHERE tenant = ? AND siret = ? AND deleted = false");
                ps.setString(1, organization);
                ps.setString(2, siret);
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
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Establishment establishment) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"establishments\"(uuid, \"name\", corporate_name, siret, sage_code, description, mail, phone, activity, entity, iban, bic, facturation_analysis, created, tenant, agency_id) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, establishment.uuid);
        ps.setString(2, establishment.name);
        ps.setString(3, establishment.corporateName);
        ps.setString(4, establishment.siret);
        ps.setString(5, establishment.sageCode);
        ps.setString(6, establishment.description.orElse(null));
        ps.setString(7, establishment.mail.orElse(null));
        ps.setString(8, establishment.phone.orElse(null));
        ps.setString(9, establishment.activity.orElse(null));
        ps.setString(10, establishment.entity);
        ps.setString(11, establishment.iban);
        ps.setString(12, establishment.bic);
        ps.setString(13, establishment.facturationAnalysis);
        ps.setTimestamp(14, new Timestamp(establishment.created.getTime()));
        ps.setString(15, organization);
        ps.setString(16, establishment.agency);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"establishments\" SET deleted = true WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Establishment establishment) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"establishments\" SET \"name\" = ?, corporate_name = ?, siret = ?, description = ?, mail = ?, phone = ?, activity = ?, entity = ?, client_modified = ?, validator_modified = ?, iban = ?, bic = ?, facturation_analysis = ?, agency_id = ? WHERE uuid = ? AND tenant = ?");
        // Set clause
        ps.setString(1, establishment.name);
        ps.setString(2, establishment.corporateName);
        ps.setString(3, establishment.siret);
        ps.setString(4, establishment.description.orElse(null));
        ps.setString(5, establishment.mail.orElse(null));
        ps.setString(6, establishment.phone.orElse(null));
        ps.setString(7, establishment.activity.orElse(null));
        ps.setString(8, establishment.entity);
        ps.setBoolean(9, establishment.clientModified);
        ps.setBoolean(10, establishment.validatorModified);
        ps.setString(11, establishment.iban);
        ps.setString(12, establishment.bic);
        ps.setString(13, establishment.facturationAnalysis);
        ps.setString(14, establishment.agency);
        // Where clause
        ps.setString(15, establishment.uuid);
        ps.setString(16, organization);
        return ps;
    }

    @Override
    public Boolean setClientExported(String organization, String establishmentId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("UPDATE \"establishments\" SET client_exported = TRUE WHERE uuid = ?");
                ps.setString(1, establishmentId);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Boolean setValidatorExported(String organization, String establishmentId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("UPDATE \"establishments\" SET validator_exported = TRUE WHERE uuid = ?");
                ps.setString(1, establishmentId);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Boolean setClientUpToDate(String organization, String establishmentId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("UPDATE \"establishments\" SET client_modified = FALSE WHERE uuid = ?");
                ps.setString(1, establishmentId);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Boolean setValidatorUpToDate(String organization, String establishmentId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("UPDATE \"establishments\" SET validator_modified = FALSE WHERE uuid = ?");
                ps.setString(1, establishmentId);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error(e.getMessage());
                return false;
            }
        });
    }

    @Override
    public List<EstablishmentWithRole> getDelegatesByRole(String organization, String uuid, String role) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_delegates\" WHERE establishment_id = ? AND role = ?");
                ps.setString(1, uuid);
                ps.setString(2, role);
                final ResultSet result = ps.executeQuery();
                final List<EstablishmentWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new EstablishmentWithRole(result.getString("delegate_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    public List<EstablishmentWithRole> getDelegates(String organization, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_delegates\" WHERE establishment_id = ?");
                ps.setString(1, uuid);
                final ResultSet result = ps.executeQuery();
                final List<EstablishmentWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new EstablishmentWithRole(result.getString("delegate_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }


    @Override
    public List<AddressWithRole> getAddressesByRole(String organization, String uuid, String role) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_addresses\" WHERE establishment_id = ? AND role = ?");
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
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_addresses\" WHERE establishment_id = ?");
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
    public List<PeopleWithRole> getPeopleByRole(String organization, String uuid, String role) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_people\" WHERE establishment_id = ? AND role = ?");
                ps.setString(1, uuid);
                ps.setString(2, role);
                final ResultSet result = ps.executeQuery();
                final List<PeopleWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new PeopleWithRole(result.getString("people_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    public List<PeopleWithRole> getPeople(String organization, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"establishment_people\" WHERE establishment_id = ?");
                ps.setString(1, uuid);
                final ResultSet result = ps.executeQuery();
                final List<PeopleWithRole> items = new ArrayList<>();
                while (result.next()) {
                    items.add(new PeopleWithRole(result.getString("people_id"), result.getString("role")));
                }
                return items;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to get item from database", e);
            }
        });
    }

    @Override
    public Boolean addDelegate(final String establishmentId, final String delegateId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("INSERT INTO \"establishment_delegates\"(establishment_id, delegate_id, role) VALUES (?, ?, ?)");
                ps.setString(1, establishmentId);
                ps.setString(2, delegateId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link delegate to establishment from database", e);
            }
        });
    }

    @Override
    public Boolean addAddress(final String establishmentId, final String addressId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("INSERT INTO \"establishment_addresses\"(establishment_id, address_id, role) VALUES (?, ?, ?)");
                ps.setString(1, establishmentId);
                ps.setString(2, addressId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link address to establishment from database", e);
            }
        });
    }

    @Override
    public Boolean addPeople(final String establishmentId, final String peopleId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("INSERT INTO \"establishment_people\"(establishment_id, people_id, role) VALUES (?, ?, ?)");
                ps.setString(1, establishmentId);
                ps.setString(2, peopleId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to link people to establishment from database", e);
            }
        });
    }

    @Override
    public Boolean removeDelegate(final String establishmentId, final String delegateId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("DELETE FROM \"establishment_delegates\" WHERE establishment_id = ? AND delegate_id = ? AND role = ?");
                ps.setString(1, establishmentId);
                ps.setString(2, delegateId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove link between delegate and establishment from database", e);
            }
        });
    }

    @Override
    public Boolean removeAddress(final String establishmentId, final String addressId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("DELETE FROM \"establishment_addresses\" WHERE establishment_id = ? AND address_id = ? AND role = ?");
                ps.setString(1, establishmentId);
                ps.setString(2, addressId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove link between address and establishment from database", e);
            }
        });
    }

    @Override
    public Boolean removePeople(final String establishmentId, final String peopleId, final String role) {
        return database.withConnection(tr -> {
            try {
                final PreparedStatement ps = tr.prepareStatement("DELETE FROM \"establishment_people\" WHERE establishment_id = ? AND people_id = ? AND role = ?");
                ps.setString(1, establishmentId);
                ps.setString(2, peopleId);
                ps.setString(3, role);
                final int res = ps.executeUpdate();
                return res > 0;
            } catch (SQLException e) {
                throw new RuntimeException("Failed to remove link between people and establishment from database", e);
            }
        });
    }

    @Override
    public List<EstablishmentComment> getComments(String organization, String uuid) {
        return database.withConnection(tr -> {
            try {
                final String sql = "SELECT * FROM \"establishments_comments\" WHERE id_establishment=?";
                final PreparedStatement ps = tr.prepareStatement(sql);
                ps.setString(1, uuid);
                final List<EstablishmentComment> commentList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while(result.next()) {
                    commentList.add(new EstablishmentComment(Optional.of(result.getString("uuid")),
                            result.getString("id_establishment"),
                            Optional.ofNullable(result.getString("id_user")),
                            result.getString("comment"),
                            new java.util.Date(result.getTimestamp("created").getTime()),
                            EventType.valueOf(result.getString("event_type"))
                            )
                    );
                }
                return commentList;
            } catch (SQLException e) {
                logger.error("Failed to get comments from account " + uuid, e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Optional<EstablishmentComment> addComment(String organization, EstablishmentComment comment) {
        return database.withConnection(tr -> {
            try {
                final String sql = "INSERT INTO \"establishments_comments\"(uuid, id_establishment, id_user, comment, created, event_type) VALUES (?,?,?,?,?,?) ";
                final PreparedStatement ps = tr.prepareStatement(sql);
                ps.setString(1, comment.uuid);
                ps.setString(2, comment.idEstablishment);
                ps.setString(3, comment.idUser.orElse(null));
                ps.setString(4, comment.comment);
                ps.setTimestamp(5, new Timestamp(comment.created.getTime()));
                ps.setString(6, comment.event.toString());
                ps.execute();
                return Optional.of(comment);
            }catch (SQLException e) {
                logger.error("Failed to add comment to establishment " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}
