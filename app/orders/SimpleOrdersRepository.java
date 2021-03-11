package orders;

import core.BaseRepository;
import core.EventType;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SimpleOrdersRepository extends BaseRepository<Order, String> implements OrdersRepository {

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(SimpleOrdersRepository.class);

    protected final Database database;

    @Inject
    SimpleOrdersRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    private ReportDestination parseReportDestination(final ResultSet result, String organization) throws SQLException {
        final String uuid = result.getString("uuid");
        final Optional<String> order = Optional.ofNullable(result.getString("order"));
        final Optional<String> mail = Optional.ofNullable(result.getString("dest_mail"));
        final Optional<String> url = Optional.ofNullable(result.getString("dest_url"));
        final Optional<String> address = Optional.ofNullable(result.getString("dest_address"));
        final Optional<String> people = Optional.ofNullable(result.getString("dest_people"));
        final Optional<String> establishment = Optional.ofNullable(result.getString("dest_establishment"));
        return new ReportDestination(Optional.of(uuid), order, mail, url, address, people, establishment);
    }

    @Override
    public List<ReportDestination> getReportDestinationsFromEstablishment(final String organization, final String establishmentId) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM report_destinations WHERE establishment=? AND tenant = ?");
                ps.setString(1, establishmentId);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                List<ReportDestination> reportDestinations = new ArrayList<>();
                while (result.next()) {
                    reportDestinations.add(parseReportDestination(result, organization));
                }
                return reportDestinations;
            } catch (SQLException e) {
                logger.error("Failed to get report destinations from establishment " + establishmentId + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<ReportDestination> getReportDestinationsFromMarket(final String organization, final String marketId) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM report_destinations WHERE market=? AND tenant = ?");
                ps.setString(1, marketId);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                List<ReportDestination> reportDestinations = new ArrayList<>();
                while (result.next()) {
                    reportDestinations.add(parseReportDestination(result, organization));
                }
                return reportDestinations;
            } catch (SQLException e) {
                logger.error("Failed to get report destinations from market " + marketId + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<ReportDestination> getReportDestinationsFromOrder(final String organization, final String orderId) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM report_destinations WHERE \"order\"=? AND tenant = ?");
                ps.setString(1, orderId);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                List<ReportDestination> reportDestinations = new ArrayList<>();
                while (result.next()) {
                    reportDestinations.add(parseReportDestination(result, organization));
                }
                return reportDestinations;
            } catch (SQLException e) {
                logger.error("Failed to get report destinations from order " + orderId + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Order parseOneItem(final ResultSet result, final String organization) throws SQLException {

        // Mandatory fields
        final String uuid = result.getString("uuid");
        final String name = result.getString("name");
        final String account = result.getString("purchaser");
        final OrderStatus status = OrderStatus.fromId(result.getString("status"));
        final Timestamp created = result.getTimestamp("created");

        // Optional fields
        final Optional<String> market = Optional.ofNullable(result.getString("market"));

        final Optional<String> estimate = Optional.ofNullable(result.getString("estimate"));

        final Optional<String> reference_number = Optional.ofNullable(result.getString("reference_number"));

        final Optional<String> reference_file = Optional.ofNullable(result.getString("reference_file"));

        final Optional<Timestamp> received = Optional.ofNullable(result.getTimestamp("received"));

        final Optional<Timestamp> deadline = Optional.ofNullable(result.getTimestamp("deadline"));

        final Optional<Timestamp> advice_visit = Optional.ofNullable(result.getTimestamp("advice_visit"));

        final Optional<Timestamp> assessment = Optional.ofNullable(result.getTimestamp("assessment"));

        final Optional<String> description = Optional.ofNullable(result.getString("description"));

        final Optional<String> workdescription = Optional.ofNullable(result.getString("workdescription"));

        final Optional<String> purchaserContact = Optional.ofNullable(result.getString("purchaser_contact"));

        final Optional<String> commercial = Optional.ofNullable(result.getString("commercial"));

        final Optional<String> establishment = Optional.ofNullable(result.getString("establishment"));

        final Optional<String> commentary = Optional.ofNullable(result.getString("commentary"));

        final Optional<java.util.Date> finalReceived = received.map(r -> new Date(r.getTime()));

        final Optional<java.util.Date> finalDeadline = deadline.map(d -> new Date(d.getTime()));

        final Optional<java.util.Date> finalAdviceVisit = advice_visit.map(a -> new Date(a.getTime()));

        final Optional<java.util.Date> finalAssessment = assessment.map(a -> new Date(a.getTime()));
        final Optional<String> agency = Optional.ofNullable(result.getString("agency"));
        final Optional<String> billedEstablishment = Optional.ofNullable(result.getString("billed_establishment"));
        final Optional<String> billedContact = Optional.ofNullable(result.getString("billed_contact"));
        final Optional<String> payerEstablishment = Optional.ofNullable(result.getString("payer_establishment"));
        final Optional<String> payerContact = Optional.ofNullable(result.getString("payer_contact"));

        return new Order(Optional.of(uuid), name, account, status, new Date(created.getTime()), market, estimate, reference_number,
                reference_file, finalReceived, finalDeadline, finalAdviceVisit, finalAssessment, description, workdescription,
                purchaserContact, commercial, establishment, commentary, agency, billedEstablishment, billedContact, payerEstablishment, payerContact);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization, final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT 100");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization, final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization, final Order order) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"orders\"(uuid, name, purchaser, status, created, market, estimate, purchaser_contact, commercial, establishment, received, agency, billed_establishment, billed_contact, payer_establishment, payer_contact, tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, order.uuid);
        ps.setString(2, order.name);
        ps.setString(3, order.account);
        ps.setString(4, order.status.getId());
        ps.setTimestamp(5, new Timestamp(order.created.getTime()));
        ps.setString(6, order.market.orElse(null));
        ps.setString(7, order.estimate.orElse(null));
        ps.setString(8, order.purchaserContact.orElse(null));
        ps.setString(9, order.commercial.orElse(null));
        ps.setString(10, order.establishment.orElse(null));
        ps.setTimestamp(11, order.received.map(r -> new Timestamp(r.getTime())).orElse(null));
        ps.setString(12, order.agency.orElse(null));
        ps.setString(13, order.billedEstablishment.orElse(null));
        ps.setString(14, order.billedContact.orElse(null));
        ps.setString(15, order.payerEstablishment.orElse(null));
        ps.setString(16, order.payerContact.orElse(null));
        ps.setString(17, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization, final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"orders\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization, final Order item) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"orders\" SET reference_number = ?, reference_file = ?, received = ?, deadline = ?, advice_visit = ?, assessment = ?, description = ?, workdescription = ?, status = ?, commentary = ?, purchaser_contact = ?, commercial = ?, agency = ?, billed_establishment = ?, billed_contact = ?, payer_establishment = ?, payer_contact = ? WHERE uuid = ? AND tenant = ?");
        ps.setString(1, item.referenceNumber.orElse(null));
        ps.setString(2, item.referenceFile.orElse(null));

        Optional<Timestamp> finalReceived = Optional.empty();
        if (item.received.isPresent()) {
            finalReceived = Optional.of(new Timestamp(item.received.get().getTime()));
        }
        ps.setTimestamp(3, finalReceived.orElse(null));

        Optional<Timestamp> finalDeadline = Optional.empty();
        if (item.deadline.isPresent()) {
            finalDeadline = Optional.of(new Timestamp(item.deadline.get().getTime()));
        }
        ps.setTimestamp(4, finalDeadline.orElse(null));

        Optional<Timestamp> finalAdviceVisit = Optional.empty();
        if (item.adviceVisit.isPresent()) {
            finalAdviceVisit = Optional.of(new Timestamp(item.adviceVisit.get().getTime()));
        }
        ps.setTimestamp(5, finalAdviceVisit.orElse(null));

        Optional<Timestamp> finalAssessment = Optional.empty();
        if (item.assessment.isPresent()) {
            finalAssessment = Optional.of(new Timestamp(item.assessment.get().getTime()));
        }
        ps.setTimestamp(6, finalAssessment.orElse(null));

        ps.setString(7, item.description.orElse(null));
        ps.setString(8, item.workdescription.orElse(null));
        ps.setString(9, item.status.getId());
        ps.setString(10, item.commentary.orElse(null));
        ps.setString(11, item.purchaserContact.orElse(null));
        ps.setString(12, item.commercial.orElse(null));
        ps.setString(13, item.agency.orElse(null));
        ps.setString(14, item.billedEstablishment.orElse(null));
        ps.setString(15, item.billedContact.orElse(null));
        ps.setString(16, item.payerEstablishment.orElse(null));
        ps.setString(17, item.payerContact.orElse(null));
        ps.setString(18, item.uuid);
        ps.setString(19, organization);
        return ps;
    }

    @Override
    public List<Order> getFromMarket(String organization, String marketUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE market=? AND tenant=?");
                ps.setString(1, marketUuid);
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from market " + marketUuid + ".", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> getFromEstimate(String organization, String estimateUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE estimate=? AND tenant=?");
                ps.setString(1, estimateUuid);
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from estimate " + estimateUuid + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> getFromEstablishment(String organization, String establishmentId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("SELECT * FROM \"orders\" WHERE establishment = ? AND tenant = ?");
                ps.setString(1, establishmentId);
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from establishment " + establishmentId + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> getFromAccount(String organization, String accountId) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("SELECT * FROM \"orders\" WHERE purchaser = ? AND tenant = ?");
                ps.setString(1, accountId);
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from account " + accountId + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> getFromList(String organization, List<String> orders) {
        return database.withConnection(connection -> {
            try {
                final PreparedStatement ps = connection.prepareStatement("SELECT * FROM \"orders\" WHERE uuid = ANY(?) AND tenant=?");
                final String[] list = orders.toArray(new String[0]);
                ps.setArray(1, connection.createArrayOf("text", list));
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from the given list from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Optional<Order> getFromBill(String organization, String billUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\",bills WHERE \"order\" = orders.uuid AND bills.uuid = ? AND bills.tenant = orders.tenant AND bills.tenant=?");
                ps.setString(1, billUuid);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get order from bill " + billUuid + " from database", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Order> getFromStatus(String organization, OrderStatus status) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE status=? AND tenant=?");
                ps.setString(1, status.getId());
                ps.setString(2, organization);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from status " + status.getId() + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> getPageFromStatus(String organization, OrderStatus status, Integer offset, Integer length) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE status=? AND tenant=? LIMIT ? OFFSET ?");
                ps.setString(1, status.getId());
                ps.setString(2, organization);
                ps.setInt(3, length);
                ps.setInt(4, offset);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from status " + status.getId() + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public List<Order> searchPageFromStatus(final String organization, final OrderStatus status, final String pattern, final Integer offset, final Integer length) {
        final List<String> tokens = Arrays.asList(pattern.trim().split(" "));
        final StringJoiner joiner = new StringJoiner(":* & ", "", ":*");
        tokens.forEach(joiner::add);
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND status=? AND tenant=? LIMIT ? OFFSET ?");
                ps.setString(1, joiner.toString());
                ps.setString(2, status.getId());
                ps.setString(3, organization);
                ps.setInt(4, length);
                ps.setInt(5, offset);
                final List<Order> orderList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    orderList.add(parseOneItem(result, organization));
                }
                return orderList;
            } catch (SQLException e) {
                logger.error("Failed to get orders from status " + status.getId() + " from database", e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Boolean setStatus(String organization, String uuid, OrderStatus status) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"orders\" SET status = ? WHERE uuid = ? AND tenant = ?");
                ps.setString(1, status.getId());
                ps.setString(2, uuid);
                ps.setString(3, organization);
                int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set status of order " + uuid + " in organization " + organization + ".", e);
                return false;
            }
        });
    }

    @Override
    public Optional<Order> checkName(String organization, String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"orders\" WHERE name=? AND tenant=?");
                ps.setString(1, name);
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.of(parseOneItem(result, organization));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get orders with name " + name + " from database", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Boolean setOrderLines(String organization, String orderUuid, List<OrderLine> orderLines, List<String> orderLinesToDelete) {
        return database.withTransaction(transaction -> {
            try {
                for (String orderLineToDelete : orderLinesToDelete) {
                    final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"order_lines\" WHERE uuid=? AND tenant=?");
                    ps.setString(1, orderLineToDelete);
                    ps.setString(2, organization);
                    ps.execute();
                }
                for (OrderLine orderLine : orderLines) {
                    final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"order_lines\" WHERE uuid=? AND tenant=?");
                    ps.setString(1, orderLine.uuid);
                    ps.setString(2, organization);
                    final ResultSet result = ps.executeQuery();
                    if (result.next()) {
                        final PreparedStatement update = transaction.prepareStatement("UPDATE \"order_lines\" SET refadx = ?, refbpu = ?, designation = ?, price = ?, quantity = ?, discount = ?, tvacode = ?, total = ?  WHERE uuid = ? AND tenant = ?");
                        update.setString(1, orderLine.refadx);
                        update.setString(2, orderLine.refbpu.orElse(null));
                        update.setString(3, orderLine.designation.orElse(null));
                        update.setBigDecimal(4, orderLine.price);
                        update.setInt(5, orderLine.quantity);
                        update.setFloat(6, orderLine.discount);
                        update.setString(7, orderLine.tvacode);
                        update.setFloat(8, orderLine.total);
                        update.setString(9, orderLine.uuid);
                        update.setString(10, organization);
                        update.execute();
                    } else {
                        final PreparedStatement insert = transaction.prepareStatement("INSERT INTO \"order_lines\"(uuid, refadx, refbpu, designation, price, quantity, discount, tvacode, total, \"order\", tenant) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                        insert.setString(1, orderLine.uuid);
                        insert.setString(2, orderLine.refadx);
                        insert.setString(3, orderLine.refbpu.orElse(null));
                        insert.setString(4, orderLine.designation.orElse(null));
                        insert.setBigDecimal(5, orderLine.price);
                        insert.setInt(6, orderLine.quantity);
                        insert.setFloat(7, orderLine.discount);
                        insert.setString(8, orderLine.tvacode);
                        insert.setFloat(9, orderLine.total);
                        insert.setString(10, orderUuid);
                        insert.setString(11, organization);
                        insert.execute();
                    }
                }
                transaction.commit();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to set order lines for order " + orderUuid + " from database", e);
                try {
                    transaction.rollback();
                } catch (SQLException throwables) {
                    logger.error("Error during rollback : ", throwables);
                }
                return false;
            }
        });
    }

    @Override
    public List<OrderLine> getOrderLines(final String organization, final String orderUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"order_lines\" WHERE tenant=? AND \"order\"=?");
                ps.setString(1, organization);
                ps.setString(2, orderUuid);
                final ResultSet result = ps.executeQuery();
                List<OrderLine> orderLines = new ArrayList<>();
                while (result.next()) {
                    final String uuid = result.getString("uuid");
                    final String refadx = result.getString("refadx");
                    final Optional<String> refbpu = Optional.ofNullable(result.getString("refbpu"));
                    final Optional<String> designation = Optional.ofNullable(result.getString("designation"));
                    final BigDecimal price = result.getBigDecimal("price");
                    final Integer quantity = result.getInt("quantity");
                    final Float discount = result.getFloat("discount");
                    final String tvacode = result.getString("tvacode");
                    final Float total = result.getFloat("total");
                    OrderLine orderLine = new OrderLine(Optional.of(uuid), refadx, refbpu, designation, price, quantity, discount, tvacode, total);
                    orderLines.add(orderLine);
                }
                return orderLines;
            } catch (SQLException e) {
                logger.error("Failed to get order lines from order " + orderUuid + " from database", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<String> getEstablishmentsWithOrder(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT DISTINCT establishment FROM \"orders\" WHERE tenant=?");
                ps.setString(1, organization);
                final List<String> establishments = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    if (result.getString("establishment") != null) {
                        establishments.add(result.getString("establishment"));
                    }
                }
                return establishments;
            } catch (SQLException e) {
                logger.error("Failed to get establishements from all orders", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<OrderComment> getComments(String organization, final String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"order_comments\" WHERE id_order=?");
                ps.setString(1, uuid);
                final List<OrderComment> commentList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    commentList.add(new OrderComment(Optional.of(result.getString("uuid")),
                            result.getString("id_order"),
                            Optional.ofNullable(result.getString("id_user")),
                            result.getString("comment"),
                            new java.util.Date(result.getTimestamp("created").getTime()),
                            EventType.valueOf(result.getString("event_type"))
                    ));
                }
                return commentList;
            } catch (SQLException e) {
                logger.error("Failed to get comments from order " + uuid, e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Boolean deleteReportDestination(String o, String i) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"report_destinations\" WHERE uuid=?");
                ps.setString(1, i);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to add comment to order " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Optional<String> addReportDestination(String o, ReportDestination n) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"report_destinations\"(uuid, dest_mail, dest_url, dest_address, dest_people, dest_establishment, \"order\", tenant) VALUES (?,?,?,?,?,?,?,?)");
                ps.setString(1, n.uuid);
                ps.setString(2, n.mail.orElse(null));
                ps.setString(3, n.url.orElse(null));
                ps.setString(4, n.address.orElse(null));
                ps.setString(5, n.people.orElse(null));
                ps.setString(6, n.establishment.orElse(null));
                ps.setString(7, n.order.orElse(null));
                ps.setString(8, o);
                ps.execute();
                return Optional.of(n.uuid);
            } catch (SQLException e) {
                logger.error("Failed to add comment to order " + e.getMessage());
                return Optional.empty();
            }
        });
    }

    @Override
    public Boolean updateReportDestination(String organization, ReportDestination n) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"report_destinations\" SET dest_mail=?, dest_url=?, dest_address=?, dest_people=?, dest_establishment=? WHERE uuid=?");
                ps.setString(1, n.mail.orElse(null));
                ps.setString(2, n.url.orElse(null));
                ps.setString(3, n.address.orElse(null));
                ps.setString(4, n.people.orElse(null));
                ps.setString(5, n.establishment.orElse(null));
                ps.setString(6, n.uuid);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to add comment to order " + e.getMessage());
                return false;
            }
        });
    }

    @Override
    public Optional<OrderComment> addComment(String organization, OrderComment comment) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"order_comments\"(uuid, id_order, id_user, comment, created, event_type)  VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, comment.uuid);
                ps.setString(2, comment.idOrder);
                ps.setString(3, comment.idUser.orElse(null));
                ps.setString(4, comment.comment);
                ps.setTimestamp(5, new Timestamp(comment.created.getTime()));
                ps.setString(6, comment.eventType.toString());
                ps.execute();
                return Optional.of(comment);
            } catch (SQLException e) {
                logger.error("Failed to add comment to order " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}
