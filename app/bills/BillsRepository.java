package bills;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BillsRepository {

    Optional<String> add(String organization, Bill bill);

    Optional<String> addPaiement(Payment payment, String factureUuid);

    Boolean addLine(String factureUuid, BillLine line);

    Optional<String> addAvoir(String factureUuid, CreditNote creditNote);

    List<CreditNote> avoirs(String facture);

    List<BillLine> factureLines(String facture);

    List<Bill> getAll(String organization);

    List<CreditNote> getAllCreditNotes(String organization);

    List<Bill> getPage(String organization, Integer offset, Integer length);

    List<Bill> getExported(String organization, Date startingDate, Date endingDate);

    List<Bill> getFromOrder(String organization, String order);

    Optional<String> getLastBillName(String organization);

    Optional<String> getLastCreditNoteName(String organization);

    List<Bill> search(String organization, String pattern);

    List<Bill> searchPage(String organization, String pattern, Integer offset, Integer length);

    Boolean setStatus(String organization, String uuid, BillStatus status);

    Optional<Bill> get(String organization, String uuid);

    Optional<Bill> getFromName(String organization, String name);

    List<Bill> getToExport(String organization);

    Optional<CreditNote> getCreditNoteFromName(String name);

    List<Payment> paiements(String facture);

    Boolean setCreditNote(BillLine ligne, String creditUuid);

    Boolean setExportDate(Bill facture, Date exportDate, Boolean changeStatus);

    Boolean setExportDate(CreditNote creditNote, Date exportDate);

    Boolean setExportDate(Payment payment, Date exportDate);

    Boolean setName(CreditNote credtNote, String name);

    Boolean setName(Bill facture, String name);

    Optional<Bill> update(String organization, Bill bill);

    Optional<String> delete(String organization, String uuid);

    List<BillComment> getComments(String organization, String uuid);

    Optional<BillComment> addComment(String organization, BillComment billComment);
}
