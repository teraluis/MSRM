package bills;

import core.BaseRepository;
import core.EventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.api.db.Database;
import play.db.NamedDatabase;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.Date;

public class SimpleBillsRepository extends BaseRepository<Bill, String> implements BillsRepository {

    protected static final Logger logger = LoggerFactory.getLogger(SimpleBillsRepository.class);

    protected final Database database;

    private void parseCreditNote(List<CreditNote> avoirs, ResultSet result) throws SQLException {
        while (result.next()) {
            final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
            final String name = result.getString("name");
            final Date date = new Date(result.getTimestamp("date").getTime());
            final Optional<Timestamp> timestamp = Optional.ofNullable(result.getTimestamp("exportdate"));
            Optional<Date> exportDate = Optional.empty();
            if (timestamp.isPresent()) {
                exportDate = Optional.of(new Date(timestamp.get().getTime()));
            }
            avoirs.add(new CreditNote(Optional.of(uuid.get()), name, date, exportDate));
        }
    }

    @Inject
    SimpleBillsRepository(@NamedDatabase("crm") Database db) {
        super(db, logger);
        this.database = db;
    }

    @Override
    public Boolean setExportDate(Payment payment, Date exportDate) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"payments\" set exportdate = ? WHERE uuid = ?");
                ps.setTimestamp(1, new Timestamp(exportDate.getTime()));
                ps.setString(2, payment.uuid);
                final int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set export date for payment " + payment.uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean setName(CreditNote creditNote, String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"creditnotes\" set \"name\" = ? WHERE uuid = ?");
                ps.setString(1, name);
                ps.setString(2, creditNote.uuid);
                final int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set export date for credit notes " + creditNote.uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean setName(Bill bill, String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"bills\" set \"name\" = ? WHERE uuid = ?");
                ps.setString(1, name);
                ps.setString(2, bill.uuid);
                final int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set export date for bill " + bill.uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean setExportDate(CreditNote creditNote, Date exportDate) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"creditnotes\" set exportdate = ? WHERE uuid = ?");
                ps.setTimestamp(1, new Timestamp(exportDate.getTime()));
                ps.setString(2, creditNote.uuid);
                final int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set export date for credit notes " + creditNote.uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public Boolean setExportDate(Bill bill, Date exportDate, Boolean changeStatus) {
        return database.withConnection(transaction -> {
            try {
                String query = changeStatus ? "UPDATE \"bills\" set exportdate = ?, \"status\" = \'billed\' WHERE uuid = ?" : "UPDATE \"bills\" set exportdate = ? WHERE uuid = ?";
                final PreparedStatement ps = transaction.prepareStatement(query);
                ps.setTimestamp(1, new Timestamp(exportDate.getTime()));
                ps.setString(2, bill.uuid);
                final int rows = ps.executeUpdate();
                return rows == 1;
            } catch (SQLException e) {
                logger.error("Failed to set export date for bill " + bill + ".", e);
                return false;
            }
        });
    }

    @Override
    public List<BillLine> factureLines(final String facture) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bill_lines\" WHERE bill = ?");
                ps.setString(1, facture);
                final List<BillLine> facturesList = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
                    final String refadx = result.getString("refadx");
                    final Optional<String> refbpu = Optional.ofNullable(result.getString("refbpu"));
                    final Optional<String> designation = Optional.ofNullable(result.getString("designation"));
                    final BigDecimal price = result.getBigDecimal("price");
                    final Integer quantity = result.getInt("quantity");
                    final Optional<String> tvaCode = Optional.ofNullable(result.getString("tvacode"));
                    final BigDecimal total = result.getBigDecimal("total");
                    final BigDecimal discount = result.getBigDecimal("discount");
                    final Timestamp billingdate = result.getTimestamp("billingdate");
                    final Optional<String> creditnote = Optional.ofNullable(result.getString("creditnote"));
                    facturesList.add(new BillLine(Optional.of(uuid.get()), refadx, refbpu, designation, tvaCode.get(), price, quantity, total, discount, new Date(billingdate.getTime()), creditnote));
                }
                return facturesList;
            } catch (SQLException e) {
                logger.error("Failed to get lines from facture " + facture + ".", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<CreditNote> getAllCreditNotes(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"creditnotes\"");
                final List<CreditNote> avoirs = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                parseCreditNote(avoirs, result);
                return avoirs;
            } catch (SQLException e) {
                logger.error("Failed to get credit notes.", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Payment> paiements(final String facture) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"payments\" WHERE bill = ? ORDER BY \"date\"");
                ps.setString(1, facture);
                final List<Payment> payments = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
                    final String type = result.getString("type");
                    final BigDecimal value = result.getBigDecimal("value");
                    final Boolean received = result.getBoolean("received");
                    final Date date = new Date(result.getTimestamp("date").getTime());
                    final Optional<Timestamp> timestamp = Optional.ofNullable(result.getTimestamp("exportdate"));
                    Optional<Date> exportDate = Optional.empty();
                    if (timestamp.isPresent()) {
                        exportDate = Optional.of(new Date(timestamp.get().getTime()));
                    }
                    payments.add(new Payment(Optional.of(uuid.get()), type, value, received, date, exportDate));
                }
                return payments;
            } catch (SQLException e) {
                logger.error("Failed to get payments from facture " + facture + ".", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<CreditNote> avoirs(final String facture) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"creditnotes\" WHERE bill = ?");
                ps.setString(1, facture);
                final List<CreditNote> creditNotes = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                parseCreditNote(creditNotes, result);
                return creditNotes;
            } catch (SQLException e) {
                logger.error("Failed to get credit notes from facture " + facture + ".", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    protected PreparedStatement buildGetAllRequest(final Connection transaction, final String organization) throws
            SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE tenant=?");
        ps.setString(1, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildGetPageRequest(final Connection transaction, final String organization,
                                                    final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, organization);
        ps.setInt(2, length);
        ps.setInt(3, offset);
        return ps;
    }

    @Override
    protected Bill parseOneItem(final ResultSet result, final String organization) throws SQLException {
        final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
        final Optional<String> name = Optional.ofNullable(result.getString("name"));
        final Boolean account = result.getBoolean("accompte");
        final Optional<String> recoverystatus = Optional.ofNullable(result.getString("recoverystatus"));
        final String order = result.getString("order");
        final Optional<Timestamp> exportdate = Optional.ofNullable(result.getTimestamp("exportdate"));
        final BillStatus status = BillStatus.fromId(result.getString("status"));
        Optional<Date> finalExportDate = Optional.empty();
        if (exportdate.isPresent()) {
            finalExportDate = Optional.of(new Date(exportdate.get().getTime()));
        }
        final Timestamp deadline = result.getTimestamp("deadline");
        return new Bill(Optional.of(uuid.get()), name.get(), account, status, recoverystatus, order, new Date(deadline.getTime()), finalExportDate);
    }

    @Override
    protected PreparedStatement buildGetRequest(final Connection transaction, final String organization,
                                                final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE tenant=? AND uuid=?");
        ps.setString(1, organization);
        ps.setString(2, id);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchRequest(final Connection transaction, final String organization,
                                                   final String query) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT 100");
        ps.setString(1, query);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildSearchPageRequest(final Connection transaction, final String organization,
                                                       final String query, final Integer offset, final Integer length) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE LOWER(\"name\")::tsvector  @@ LOWER(?)::tsquery AND tenant=? LIMIT ? OFFSET ?");
        ps.setString(1, query);
        ps.setString(2, organization);
        ps.setInt(3, length);
        ps.setInt(4, offset);
        return ps;
    }

    @Override
    protected PreparedStatement buildAddRequest(final Connection transaction, final String organization,
                                                final Bill bill) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"bills\"(uuid, name, accompte, status, recoverystatus, deadline, \"order\", tenant) values(?, ?, ?, ?, ?, ?, ?, ?)");
        ps.setString(1, bill.uuid);
        ps.setString(2, bill.name);
        ps.setBoolean(3, bill.accompte);
        ps.setString(4, bill.status.getId());
        ps.setString(5, bill.recoverystatus.orElse(null));
        ps.setTimestamp(6, new Timestamp(bill.deadline.getTime()));
        ps.setString(7, bill.order);
        ps.setString(8, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildDeleteRequest(final Connection transaction, final String organization,
                                                   final String id) throws SQLException {
        final PreparedStatement ps = transaction.prepareStatement("DELETE FROM \"bills\" WHERE uuid = ? AND tenant = ?");
        ps.setString(1, id);
        ps.setString(2, organization);
        return ps;
    }

    @Override
    protected PreparedStatement buildUpdateRequest(final Connection transaction, final String organization,
                                                   final Bill item) throws SQLException {

        final PreparedStatement ps = transaction.prepareStatement("UPDATE \"bills\" SET deadline = ?, status = ?, name = ? WHERE uuid = ? AND tenant = ?");
        ps.setTimestamp(1, new Timestamp(item.deadline.getTime()));
        ps.setString(2, item.status.getId());
        ps.setString(3, item.name);
        ps.setString(4, item.uuid);
        ps.setString(5, organization);
        return ps;
    }

    @Override
    public Optional<String> addPaiement(Payment payment, String factureUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"payments\"(uuid, \"type\", \"value\", received, \"date\", bill) values(?, ?, ?, ?, ?, ?)");
                ps.setString(1, payment.uuid);
                ps.setString(2, payment.type);
                ps.setBigDecimal(3, payment.value);
                ps.setBoolean(4, payment.received);
                ps.setTimestamp(5, new Timestamp(payment.date.getTime()));
                ps.setString(6, factureUuid);
                ps.execute();
                return Optional.of(payment.uuid);
            } catch (SQLException e) {
                logger.error("Failed to add payment for facture " + factureUuid + ".", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Boolean addLine(String factureUuid, BillLine line) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"bill_lines\"(uuid, refadx, refbpu, designation, price, quantity, discount, tvacode, total, billingdate, bill) values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
                ps.setString(1, line.uuid);
                ps.setString(2, line.refadx);
                ps.setString(3, line.refbpu.orElse(null));
                ps.setString(4, line.designation.orElse(null));
                ps.setBigDecimal(5, line.price);
                ps.setInt(6, line.quantity);
                ps.setBigDecimal(7, line.discount);
                ps.setString(8, line.tvacode);
                ps.setBigDecimal(9, line.total);
                ps.setTimestamp(10, new Timestamp(line.billingdate.getTime()));
                ps.setString(11, factureUuid);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to add bill line for bill " + factureUuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public List<Bill> getExported(String organization, Date startingDate, Date endingDate) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE exportdate > ? AND exportdate < ? AND tenant = ?");
                ps.setTimestamp(1, new Timestamp(startingDate.getTime()));
                ps.setTimestamp(2, new Timestamp(endingDate.getTime()));
                ps.setString(3, organization);
                final List<Bill> bills = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    bills.add(parseOneItem(result, organization));
                }
                return bills;
            } catch (SQLException e) {
                logger.error("Failed to get exported bills in organization " + organization + ".", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public List<Bill> getFromOrder(String organization, String order) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE \"order\" = ? AND tenant = ?");
                ps.setString(1, order);
                ps.setString(2, organization);
                final List<Bill> bills = new ArrayList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    bills.add(parseOneItem(result, organization));
                }
                return bills;
            } catch (SQLException e) {
                logger.error("Failed to get bills from order " + order + ".", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Boolean setStatus(String organization, String uuid, BillStatus status) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"bills\" SET status = ? WHERE uuid = ? AND tenant = ?");
                ps.setString(1, status.getId());
                ps.setString(2, uuid);
                ps.setString(3, organization);
                Integer row = ps.executeUpdate();
                return row == 1;
            } catch (SQLException e) {
                logger.error("Failed to set status for bill " + uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public Optional<String> getLastBillName(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT MAX(\"name\") as \"name\" FROM \"bills\" WHERE tenant = ?");
                ps.setString(1, organization);
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.ofNullable(result.getString("name"));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get max bill name.", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<String> getLastCreditNoteName(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT MAX(\"name\") as \"name\" FROM \"creditnotes\"");
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    return Optional.ofNullable(result.getString("name"));
                } else {
                    return Optional.empty();
                }
            } catch (SQLException e) {
                logger.error("Failed to get max credit notes name.", e);
                return Optional.empty();
            }
        });
    }


    @Override
    public Optional<Bill> getFromName(String organization, String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE \"name\" = ? AND tenant = ?");
                ps.setString(1, name);
                ps.setString(2, organization);
                Optional<Bill> facture = Optional.empty();
                final ResultSet result = ps.executeQuery();
                if (result.next()) {
                    facture = Optional.of(parseOneItem(result, organization));
                }
                return facture;
            } catch (SQLException e) {
                logger.error("Failed to get bill from name " + name + ".", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public List<Bill> getToExport(String organization) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills\" WHERE \"status\" = ? AND tenant = ?");
                ps.setString(1, BillStatus.CONFIRMED.getId());
                ps.setString(2, organization);
                final ResultSet result = ps.executeQuery();
                final List<Bill> bills = new ArrayList<>();
                while (result.next()) {
                    bills.add(parseOneItem(result, organization));
                }
                final PreparedStatement ps2 = transaction.prepareStatement("SELECT \"bills\".* FROM \"bills\",\"creditnotes\" WHERE bill = bills.uuid AND creditnotes.exportdate is NULL AND tenant = ?");
                ps2.setString(1, organization);
                final ResultSet result2 = ps2.executeQuery();
                while (result2.next()) {
                    bills.add(parseOneItem(result2, organization));
                }
                final PreparedStatement ps3 = transaction.prepareStatement("SELECT \"bills\".* FROM \"bills\",\"payments\" WHERE bill = bills.uuid AND payments.exportdate is NULL AND tenant = ?");
                ps3.setString(1, organization);
                final ResultSet result3 = ps3.executeQuery();
                while (result3.next()) {
                    bills.add(parseOneItem(result3, organization));
                }
                return bills;
            } catch (SQLException e) {
                logger.error("Failed to get bills to export.", e);
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Optional<CreditNote> getCreditNoteFromName(String name) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"creditnotes\" WHERE \"name\" = ?");
                ps.setString(1, name);
                final ResultSet result = ps.executeQuery();
                Optional<CreditNote> creditNote = Optional.empty();
                if (result.next()) {
                    final Optional<String> uuid = Optional.ofNullable(result.getString("uuid"));
                    final Date date = new Date(result.getTimestamp("date").getTime());
                    final Optional<Timestamp> timestamp = Optional.ofNullable(result.getTimestamp("exportdate"));
                    Optional<Date> exportDate = Optional.empty();
                    if (timestamp.isPresent()) {
                        exportDate = Optional.of(new Date(timestamp.get().getTime()));
                    }
                    creditNote = Optional.of(new CreditNote(uuid, name, date, exportDate));
                }
                return creditNote;
            } catch (SQLException e) {
                logger.error("Failed to get credit note from name " + name + ".", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Optional<String> addAvoir(String factureUuid, CreditNote creditNote) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"creditnotes\"(uuid, name, date, bill) values(?, ?, ?, ?)");
                ps.setString(1, creditNote.uuid);
                ps.setString(2, creditNote.name);
                ps.setTimestamp(3, new Timestamp(creditNote.date.getTime()));
                ps.setString(4, factureUuid);
                ps.execute();
                return Optional.of(creditNote.uuid);
            } catch (SQLException e) {
                logger.error("Failed to add credit note for bill " + factureUuid + ".", e);
                return Optional.empty();
            }
        });
    }

    @Override
    public Boolean setCreditNote(final BillLine ligne, final String creditUuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("UPDATE \"bill_lines\" SET creditnote = ? where uuid = ?");
                ps.setString(1, creditUuid);
                ps.setString(2, ligne.uuid);
                ps.execute();
                return true;
            } catch (SQLException e) {
                logger.error("Failed to update bill line " + ligne.uuid + ".", e);
                return false;
            }
        });
    }

    @Override
    public List<BillComment> getComments(String organization, final String uuid) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("SELECT * FROM \"bills_comments\" WHERE id_bill=?");
                ps.setString(1, uuid);
                final List<BillComment> commentList = new LinkedList<>();
                final ResultSet result = ps.executeQuery();
                while (result.next()) {
                    commentList.add(new BillComment(Optional.of(result.getString("uuid")),
                            result.getString("id_bill"),
                            Optional.ofNullable(result.getString("id_user")),
                            result.getString("comment"),
                            new java.util.Date(result.getTimestamp("created").getTime()),
                            EventType.valueOf(result.getString("event_type"))));
                }
                return commentList;
            } catch (SQLException e) {
                logger.error("Failed to get comments from bill " + uuid, e);
                return new LinkedList<>();
            }
        });
    }

    @Override
    public Optional<BillComment> addComment(String organization, BillComment billComment) {
        return database.withConnection(transaction -> {
            try {
                final PreparedStatement ps = transaction.prepareStatement("INSERT INTO \"bills_comments\" (uuid, id_bill, id_user, comment, created, event_type) VALUES (?, ?, ?, ?, ?, ?)");
                ps.setString(1, billComment.uuid);
                ps.setString(2, billComment.idBill);
                ps.setString(3, billComment.idUser.orElse(null));
                ps.setString(4, billComment.comment);
                ps.setTimestamp(5, new Timestamp(billComment.created.getTime()));
                ps.setString(6, billComment.event.toString());
                ps.execute();
                return Optional.of(billComment);
            } catch (SQLException e) {
                logger.error("Failed to add comment to bill " + e.getMessage());
                return Optional.empty();
            }
        });
    }
}
