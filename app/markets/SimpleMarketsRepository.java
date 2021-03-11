package markets;

import core.BaseRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

public class SimpleMarketsRepository extends BaseRepository<SimpleMarket, String> implements MarketsRepository {

    protected final Database database;
    protected static final Logger logger = LoggerFactory.getLogger(SimpleMarketsRepository.class);

    @Inject
    SimpleMarketsRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    public List<SimpleMarket> getFromEstablishment(final String organisation, final String establishmentId) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM markets WHERE uuid IN (SELECT markets_id FROM markets_establishments WHERE establishment_id = ?)");
                ps.setString(1, establishmentId);

                final ResultSet result = ps.executeQuery();

                final List<SimpleMarket> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneItem(result, organisation));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get MarketEstablishment from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<SimpleMarketEstablishment> getMarketEstablishmentByMarket(String uuid) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM markets_establishments WHERE markets_id = ?");
                ps.setString(1, uuid);

                final ResultSet result = ps.executeQuery();

                final List<SimpleMarketEstablishment> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneMarketEstablishment(result));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get MarketEstablishment from database", e);
                return new ArrayList<>();
            }
        });
    }

    private SimpleMarketEstablishment parseOneMarketEstablishment(ResultSet result) throws SQLException {
        try {
            return new SimpleMarketEstablishment(
                    result.getString("establishment_id"),
                    result.getString("role")
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SimpleMarketPeople> getMarketPeopleByMarket(String uuid) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM markets_people WHERE markets_id = ?");
                ps.setString(1, uuid);

                final ResultSet result = ps.executeQuery();

                final List<SimpleMarketPeople> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneMarketPeople(result));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get MarketEstablishment from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<SimpleMarketPeople> getPeopleByRole(String organization, String marketId, String role) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM markets_people WHERE markets_id = ? AND role = ?");
                ps.setString(1, marketId);
                ps.setString(2, role);
                final ResultSet result = ps.executeQuery();
                final List<SimpleMarketPeople> items = new ArrayList<>();
                while (result.next()) {
                    items.add(parseOneMarketPeople(result));
                }
                return items;
            } catch (SQLException e) {
                logger.error("Failed to get MarketPeople from database", e);
                return new ArrayList<>();
            }
        });
    }

    private SimpleMarketPeople parseOneMarketPeople(ResultSet result) {
        try {
            return new SimpleMarketPeople(
                    result.getString("people_id"),
                    result.getString("role")
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    public List<SimpleMarketUser> getMarketUserByMarket(String uuid) {
        return database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("SELECT * FROM markets_users WHERE markets_id = ?");
                ps.setString(1, uuid);

                final ResultSet result = ps.executeQuery();

                final List<SimpleMarketUser> items = new ArrayList<>();

                while (result.next()) {
                    items.add(parseOneMarketUser(result));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get MarketEstablishment from database", e);
                return new ArrayList<>();
            }
        });
    }

    private SimpleMarketUser parseOneMarketUser(ResultSet result) {
        try {
            return new SimpleMarketUser(
                    result.getString("users_id"),
                    result.getString("role")
            );
        } catch (SQLException throwables) {
            throwables.printStackTrace();
            return null;
        }
    }

    @Override
    protected SimpleMarket parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final String marketNumber = result.getString("market_number");
        final Optional<String> status = Optional.ofNullable(result.getString("status"));
        final String customerRequirement = result.getString("customer_requirement");
        final String agency = result.getString("agencie_id");
        final String tenant = result.getString("tenant");
        final LocalDate receiveDate = result.getDate("receive_date") != null ? result.getDate("receive_date").toLocalDate() : null;
        final LocalDate responseDate = result.getDate("response_date") != null ? result.getDate("response_date").toLocalDate() : null;
        final LocalDate returnDate = result.getDate("return_date") != null ? result.getDate("return_date").toLocalDate() : null;
        final LocalDate startDate = result.getDate("start_date") != null ? result.getDate("start_date").toLocalDate() : null;
        final Integer duration = result.getInt("duration");
        final String publicationNumber = result.getString("publication_number");
        final String origin = result.getString("origin");
        final String estimateVolume = result.getString("estimate_volume");
        final String missionOrderType = result.getString("mission_order_type");
        final String deadlineModality = result.getString("deadline_modality");
        final String dunningModality = result.getString("dunning_modality");
        final String interventionCondition = result.getString("intervention_condition");
        final String specificReportNaming = result.getString("specific_report_naming");
        final String specificReportDisplay = result.getString("specific_report_display");
        final String specificBilling = result.getString("specific_billing");
        final String missionOrderBillingLink = result.getString("mission_order_billing_link");
        final String billingFrequency = result.getString("billing_frequency");
        final String warningPoint = result.getString("warning_point");
        final String description = result.getString("description");
        final String facturationAnalysis = result.getString("facturation_analysis");
        return new SimpleMarket(
                Optional.of(uuid),
                name,
                marketNumber,
                status,
                customerRequirement,
                agency,
                tenant,
                facturationAnalysis,
                Optional.ofNullable(receiveDate),
                Optional.ofNullable(responseDate),
                Optional.ofNullable(returnDate),
                Optional.ofNullable(startDate),
                Optional.ofNullable(duration),
                Optional.ofNullable(publicationNumber),
                Optional.ofNullable(origin),
                Optional.ofNullable(estimateVolume),
                Optional.ofNullable(missionOrderType),
                Optional.ofNullable(deadlineModality),
                Optional.ofNullable(dunningModality),
                Optional.ofNullable(interventionCondition),
                Optional.ofNullable(specificReportNaming),
                Optional.ofNullable(specificReportDisplay),
                Optional.ofNullable(specificBilling),
                Optional.ofNullable(missionOrderBillingLink),
                Optional.ofNullable(billingFrequency),
                Optional.ofNullable(warningPoint),
                Optional.ofNullable(description)
        );
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        PreparedStatement ps;
        if (organization != null) {
            ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE tenant=? AND uuid=?");
            ps.setString(1, organization);
        } else {
            ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE uuid=?");
        }
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT 100");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"markets\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final SimpleMarket market) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"markets\"(uuid, name, market_number, status, tenant, facturation_analysis) values(?, ?, ?, ?, ?, ?)");
        ps.setString(1, market.uuid);
        ps.setString(2, market.name);
        ps.setString(3, market.marketNumber);
        ps.setString(4, market.status.orElse(null));
        ps.setString(5, organization);
        ps.setString(6, market.facturationAnalysis);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"markets\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final SimpleMarket item) throws SQLException {
        throw new RuntimeException("Not implemented yet");
    }

    @Override
    public List<Bpu> getBpuByMarket(String market) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bpu\" WHERE market_id = ?");
                ps.setString(1, market);

                List<Bpu> items = new ArrayList<>();

                final ResultSet result = ps.executeQuery();

                while (result.next()) {
                    items.add(new Bpu(
                            Optional.ofNullable(result.getString("uuid")),
                            result.getString("file"),
                            result.getString("market_id")
                    ));
                }

                return items;
            } catch (SQLException e) {
                logger.error("Failed to get bpu by market from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Optional<Bpu> getBpu(String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bpu\" WHERE uuid = ?");
                ps.setString(1, uuid);

                final ResultSet result = ps.executeQuery();

                if (result.next()) {
                    return Optional.of(new Bpu(
                            Optional.ofNullable(result.getString("uuid")),
                            result.getString("file"),
                            result.getString("market_id")
                    ));
                }

                return Optional.empty();
            } catch (SQLException e) {
                logger.error("Failed to get bpu by market from database", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<String> addMarket(final String organization, final Market market) {
        return database.withTransaction(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"markets\"(uuid, name, market_number, " +
                        "status, tenant, agencie_id, receive_date, response_date, return_date, start_date, " +
                        "duration, publication_number, customer_requirement, origin, estimate_volume, mission_order_type, " +
                        "deadline_modality, dunning_modality, intervention_condition, specific_report_naming, specific_report_display, " +
                        "specific_billing, mission_order_billing_link, billing_frequency, warning_point, description, facturation_analysis) " +
                        "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, market.uuid);
                ps.setString(2, market.name);
                ps.setString(3, market.marketNumber);
                ps.setString(4, market.status.orElse(null));
                ps.setString(5, organization);
                ps.setString(6, market.agency.uuid);
                ps.setDate(7, market.receiveDate.map(Date::valueOf).orElse(null));
                ps.setDate(8, market.responseDate.map(Date::valueOf).orElse(null));
                ps.setDate(9, market.returnDate.map(Date::valueOf).orElse(null));
                ps.setDate(10, market.startDate.map(Date::valueOf).orElse(null));
                ps.setInt(11, market.duration.orElse(0));
                ps.setString(12, market.publicationNumber.orElse(null));
                ps.setString(13, market.customerRequirement);
                ps.setString(14, market.origin.orElse(null));
                ps.setString(15, market.estimateVolume.orElse(null));
                ps.setString(16, market.missionOrderType.orElse(null));
                ps.setString(17, market.deadlineModality.orElse(null));
                ps.setString(18, market.dunningModality.orElse(null));
                ps.setString(19, market.interventionCondition.orElse(null));
                ps.setString(20, market.specificReportNaming.orElse(null));
                ps.setString(21, market.specificReportDisplay.orElse(null));
                ps.setString(22, market.specificBilling.orElse(null));
                ps.setString(23, market.missionOrderBillingLink.orElse(null));
                ps.setString(24, market.billingFrequency.orElse(null));
                ps.setString(25, market.warningPoint.orElse(null));
                ps.setString(26, market.description.orElse(null));
                ps.setString(27, market.facturationAnalysis);

                ps.execute();
                ps.close();

                Optional<String> id = Optional.ofNullable(market.getId());
                if (id.isPresent()) {
                    if (market.marketEstablishments != null && !market.marketEstablishments.isEmpty()) {
                        ps = transaction.prepareStatement("INSERT INTO \"markets_establishments\"(markets_id, establishment_id, role) values(?, ?, ?)");
                        for (MarketEstablishment marketEstablishment : market.marketEstablishments) {
                            ps.setString(1, id.get());
                            ps.setString(2, marketEstablishment.establishment.uuid);
                            ps.setString(3, marketEstablishment.role);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        ps.close();
                    }

                    if (market.marketPeoples != null && !market.marketPeoples.isEmpty()) {
                        ps = transaction.prepareStatement("INSERT INTO \"markets_people\"(markets_id, people_id, role) values(?, ?, ?)");
                        for (MarketPeople marketPeople : market.marketPeoples) {
                            if (marketPeople.people != null) {
                                ps.setString(1, id.get());
                                ps.setString(2, marketPeople.people.uuid);
                                ps.setString(3, marketPeople.role);
                                ps.addBatch();
                            }
                        }
                        ps.executeBatch();
                        ps.close();
                    }

                    if (market.marketUsers != null && !market.marketUsers.isEmpty()) {
                        ps = transaction.prepareStatement("INSERT INTO \"markets_users\"(markets_id, users_id, role) values(?, ?, ?)");
                        for (MarketUser marketUser : market.marketUsers) {
                            ps.setString(1, id.get());
                            ps.setString(2, marketUser.user.login);
                            ps.setString(3, marketUser.role);
                            ps.addBatch();
                        }
                        ps.executeBatch();
                        ps.close();
                    }
                }
                return id;
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
                try {
                    transaction.rollback();
                } catch (SQLException throwables) {
                    logger.error("Error during rollback : ", throwables);
                }
                return Optional.empty();
            }
        });
    }

    @Override
    public void updateMarket(Market market) {
        database.withConnection(transaction -> {
            try {
                PreparedStatement ps = transaction.prepareStatement("UPDATE \"markets\" SET name = ?, market_number = ?, " +
                        "status = ?, tenant = ?, agencie_id = ?, receive_date = ?, response_date = ?, return_date = ?, start_date = ?, " +
                        "duration = ?, publication_number = ?, customer_requirement = ?, origin = ?, estimate_volume = ?, mission_order_type = ?, " +
                        "deadline_modality = ?, dunning_modality = ?, intervention_condition = ?, specific_report_naming = ?, specific_report_display = ?, " +
                        "specific_billing = ?, mission_order_billing_link = ?, billing_frequency = ?, warning_point = ?, description = ?, facturation_analysis = ? where uuid = ?");
                ps.setString(1, market.name);
                ps.setString(2, market.marketNumber);
                ps.setString(3, market.status.orElse(null));
                ps.setString(4, market.tenant);
                ps.setString(5, market.agency.uuid);
                ps.setDate(6, market.receiveDate.map(Date::valueOf).orElse(null));
                ps.setDate(7, market.responseDate.map(Date::valueOf).orElse(null));
                ps.setDate(8, market.returnDate.map(Date::valueOf).orElse(null));
                ps.setDate(9, market.startDate.map(Date::valueOf).orElse(null));
                ps.setInt(10, market.duration.orElse(0));
                ps.setString(11, market.publicationNumber.orElse(null));
                ps.setString(12, market.customerRequirement);
                ps.setString(13, market.origin.orElse(null));
                ps.setString(14, market.estimateVolume.orElse(null));
                ps.setString(15, market.missionOrderType.orElse(null));
                ps.setString(16, market.deadlineModality.orElse(null));
                ps.setString(17, market.dunningModality.orElse(null));
                ps.setString(18, market.interventionCondition.orElse(null));
                ps.setString(19, market.specificReportNaming.orElse(null));
                ps.setString(20, market.specificReportDisplay.orElse(null));
                ps.setString(21, market.specificBilling.orElse(null));
                ps.setString(22, market.missionOrderBillingLink.orElse(null));
                ps.setString(23, market.billingFrequency.orElse(null));
                ps.setString(24, market.warningPoint.orElse(null));
                ps.setString(25, market.description.orElse(null));
                ps.setString(26, market.uuid);
                ps.setString(27, market.facturationAnalysis);

                ps.execute();

            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }
            return null;
        });
    }

    @Override
    public Boolean addContact(MarketPeople marketPeople, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"markets_people\"(markets_id, people_id, role) values(?, ?, ?)");
                ps.setString(1, uuid);
                ps.setString(2, marketPeople.people.uuid);
                ps.setString(3, marketPeople.role);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
                return false;
            }
        });
    }

    @Override
    public void updateContact(MarketPeople marketPeople, String uuid, String peopleUuid, String oldRole) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"markets_people\" SET role = ?, people_id = ? WHERE markets_id = ? AND people_id = ? AND role = ?");
                ps.setString(1, marketPeople.role);
                ps.setString(2, marketPeople.people.uuid);
                ps.setString(3, uuid);
                ps.setString(4, peopleUuid);
                ps.setString(5, oldRole);

                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public void deleteContact(String uuid, String peopleUuid, String role) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"markets_people\" WHERE markets_id = ? AND people_id = ? AND role = ?");
                ps.setString(1, uuid);
                ps.setString(2, peopleUuid);
                ps.setString(3, role);

                ps.execute();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public Boolean addEstablishment(MarketEstablishment marketEstablishment, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"markets_establishments\"(markets_id, establishment_id, role) values(?, ?, ?)");
                ps.setString(1, uuid);
                ps.setString(2, marketEstablishment.establishment.uuid);
                ps.setString(3, marketEstablishment.role);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
                return false;
            }
        });
    }

    @Override
    public void updateEstablishment(MarketEstablishment marketEstablishment, String uuid, String establishmentUuid) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"markets_establishments\" SET role = ?, establishment_id = ? WHERE markets_id = ? AND establishment_id = ?");
                ps.setString(1, marketEstablishment.role);
                ps.setString(2, marketEstablishment.establishment.uuid);
                ps.setString(3, uuid);
                ps.setString(4, establishmentUuid);

                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public void deleteAccount(String uuid, String establishmentId, String role) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"markets_establishments\" WHERE markets_id = ? AND establishment_id = ? AND role = ?");
                ps.setString(1, uuid);
                ps.setString(2, establishmentId);
                ps.setString(3, role);

                ps.execute();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public void addUser(MarketUser marketUser, String uuid) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"markets_users\"(markets_id, users_id, role) values(?, ?, ?)");
                ps.setString(1, uuid);
                ps.setString(2, marketUser.user.login);
                ps.setString(3, marketUser.role);
                ps.execute();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public void updateUser(MarketUser marketUser, String uuid, String userUuid) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"markets_users\" SET role = ?, users_id = ? WHERE markets_id = ? AND users_id = ?");
                ps.setString(1, marketUser.role);
                ps.setString(2, marketUser.user.login);
                ps.setString(3, uuid);
                ps.setString(4, userUuid);

                ps.executeUpdate();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public void deleteUser(String uuid, String userLogin) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"markets_users\" WHERE markets_id = ? AND users_id = ?");
                ps.setString(1, uuid);
                ps.setString(2, userLogin);

                ps.execute();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public Optional<String> addReferenceIfNotExist(String uuid, BpuReference reference) {
        Optional<BpuReference> ref = database.withConnection(transaction -> {
            if (reference.reference.isPresent() && reference.designation.isPresent()) {
                try {
                    final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_references\" WHERE reference = ? AND designation = ? AND market=?");
                    ps.setString(1, reference.reference.get());
                    ps.setString(2, reference.designation.get());
                    ps.setString(3, uuid);

                    final ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        return Optional.of(new BpuReference(reference.reference, reference.designation, result.getBigDecimal("price")));
                    } else {
                        return Optional.empty();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to get reference " + reference + " for market " + uuid + " from database", e);
                    return Optional.empty();
                }
            } else if (reference.reference.isPresent()) {
                try {
                    final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_references\" WHERE reference = ? AND market=? AND designation IS NULL");
                    ps.setString(1, reference.reference.get());
                    ps.setString(2, uuid);

                    final ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        return Optional.of(new BpuReference(reference.reference, reference.designation, result.getBigDecimal("price")));
                    } else {
                        return Optional.empty();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to get reference " + reference + " for market " + uuid + " from database", e);
                    return Optional.empty();
                }
            } else if (reference.designation.isPresent()) {
                try {
                    final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_references\" WHERE designation = ? AND market=? AND reference IS NULL");
                    ps.setString(1, reference.designation.get());
                    ps.setString(2, uuid);

                    final ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        return Optional.of(new BpuReference(reference.reference, reference.designation, result.getBigDecimal("price")));
                    } else {
                        return Optional.empty();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to get reference " + reference + " for market " + uuid + " from database", e);
                    return Optional.empty();
                }
            } else {
                return Optional.empty();
            }
        });
        if (!ref.isPresent()) {
            return database.withConnection(transaction -> {
                try {
                    final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"market_references\"(reference, designation, market, price) VALUES (?, ?, ?, ?)");
                    ps.setString(1, reference.reference.orElse(null));
                    ps.setString(2, reference.designation.orElse(null));
                    ps.setString(3, uuid);
                    ps.setBigDecimal(4, reference.price);
                    ps.execute();
                    return reference.reference;
                } catch (SQLException e) {
                    logger.error("Failed to insert reference " + reference + " for market " + uuid + " from database", e);
                    return Optional.empty();
                }
            });
        } else if (ref.get().price.compareTo(reference.price) != 0) {
            return database.withConnection(transaction -> {
                try {
                    if (ref.get().reference.isPresent() && ref.get().designation.isPresent()) {
                        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"market_references\" SET price = ? WHERE reference = ? AND designation = ? AND market = ?");
                        ps.setBigDecimal(1, reference.price);
                        ps.setString(2, reference.reference.get());
                        ps.setString(3, reference.designation.get());
                        ps.setString(4, uuid);
                        ps.execute();
                        return reference.reference;
                    } else if (ref.get().reference.isPresent()) {
                        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"market_references\" SET price = ? WHERE reference = ? AND designation IS NULL AND market = ?");
                        ps.setBigDecimal(1, reference.price);
                        ps.setString(2, reference.reference.get());
                        ps.setString(3, uuid);
                        ps.execute();
                        return reference.reference;
                    } else if (ref.get().designation.isPresent()) {
                        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"market_references\" SET price = ? WHERE reference IS NULL AND designation = ? AND market = ?");
                        ps.setBigDecimal(1, reference.price);
                        ps.setString(2, reference.designation.get());
                        ps.setString(3, uuid);
                        ps.execute();
                        return reference.reference;
                    } else {
                        return Optional.empty();
                    }
                } catch (SQLException e) {
                    logger.error("Failed to insert reference " + reference + " for market " + uuid + " from database", e);
                    return Optional.empty();
                }
            });
        } else {
            return ref.get().reference;
        }
    }

    @Override
    public List<BpuReference> getReferences(String uuid, String searchString) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_references\" WHERE reference LIKE ? AND market = ?");
                ps.setString(1, "%" + searchString + "%");
                ps.setString(2, uuid);

                final ResultSet result = ps.executeQuery();
                final List<BpuReference> references = new ArrayList<>();
                while (result.next()) {
                    references.add(new BpuReference(Optional.ofNullable(result.getString("reference")), Optional.ofNullable(result.getString("designation")), result.getBigDecimal("price")));
                }
                return references;
            } catch (SQLException e) {
                logger.error("Failed to get references for market " + uuid + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<BpuReference> getReferencesFromDesignation(String uuid, String searchString) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_references\" WHERE designation LIKE ? AND market = ?");
                ps.setString(1, "%" + searchString + "%");
                ps.setString(2, uuid);

                final ResultSet result = ps.executeQuery();
                final List<BpuReference> references = new ArrayList<>();
                while (result.next()) {
                    references.add(new BpuReference(Optional.ofNullable(result.getString("reference")), Optional.ofNullable(result.getString("designation")), result.getBigDecimal("price")));
                }
                return references;
            } catch (SQLException e) {
                logger.error("Failed to get references for market " + uuid + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Optional<String> addBpu(String organization, Bpu bpu) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"bpu\"(uuid, file, market_id, tenant) values(?, ?, ?, ?)");
                ps.setString(1, bpu.uuid);
                ps.setString(2, bpu.file);
                ps.setString(3, bpu.market_id);
                ps.setString(4, organization);
                ps.execute();
                return Optional.ofNullable(bpu.getId());
            } catch (SQLException e) {
                logger.error(String.format("Failed to get references for bpu %s from database", bpu.uuid), e);
                return Optional.empty();
            }
        });
    }

    @Override
    public void deleteBpu(String uuid) {
        database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"bpu\" WHERE uuid = ?");
                ps.setString(1, uuid);

                ps.execute();
            } catch (SQLException e) {
                logger.error("SQL ERROR", e);
            }

            return null;
        });
    }

    @Override
    public List<MarketComment> getComments(String organization, String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"market_comments\" WHERE id_market=?");
                ps.setString(1, uuid);
                final List<MarketComment> commentList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    commentList.add(new MarketComment(Optional.of(result.getString("uuid")),
                            result.getString("id_market"),
                            Optional.of(result.getString("id_user")),
                            result.getString("comment"),
                            new java.util.Date(result.getTimestamp("created").getTime())));
                }
                return commentList;
            } catch (SQLException e) {
                logger.error("Failed to get comments from market " + uuid, e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Optional<MarketComment> addComment(String organization, MarketComment marketComment) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"market_comments\" (uuid, id_market, id_user, comment, created) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, marketComment.uuid);
                ps.setString(2, marketComment.idMarket);
                ps.setString(3, marketComment.idUser.orElse(null));
                ps.setString(4, marketComment.comment);
                ps.setTimestamp(5, new Timestamp(marketComment.created.getTime()));
                ps.execute();
                return Optional.of(marketComment);
            } catch (SQLException e) {
                logger.error("Failed to add comment to market " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}
