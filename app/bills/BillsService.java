package bills;

import api.v1.models.Account;
import api.v1.models.Agency;
import api.v1.models.Facture;
import api.v1.models.Order;
import core.search.Pageable;
import core.search.PaginatedResult;
import estateWithAddress.EstateWithAddress;
import missionclient.Asbestos;
import missionclient.interventions.MaterializedIntervention;
import pdf.pdfdocument.File;
import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple6;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface BillsService {

    CompletionStage<Optional<String>> add(String organization, Bill facture);

    CompletionStage<Optional<String>> addPaiement(String organization, Payment paiement, Bill facture);

    // CompletionStage<Boolean> generateNames(String organization);

    CompletionStage<List<Bill>> getAll(String organization);

    CompletionStage<List<Tuple6<Facture, Order, Account, List<MaterializedIntervention>, List<EstateWithAddress>, Agency>>> getBillsToExport(String organization);

    CompletionStage<List<Tuple3<Facture, api.v1.models.Order, List<Tuple3<MaterializedIntervention, EstateWithAddress, List<Asbestos>>>>>> getExportedBills(String organization, Date startDate, Date endDate);

    CompletionStage<List<Bill>> getFromOrder(String organization, String order);

    CompletionStage<List<Bill>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Bill>> search(String organization, String pattern);

    CompletionStage<List<Bill>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Boolean> setStatus(String organization, String uuid, BillStatus status, Optional<String> login);

    CompletionStage<Optional<Bill>> get(String organization, String uuid);

    CompletionStage<Optional<Bill>> getFromName(String organization, String name);

    CompletionStage<Optional<CreditNote>> getCreditNoteFromName(String name);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<api.v1.models.Facture> serialize(String organization, Bill facture, Optional<Tuple2<Date, Date>> exportDate, Boolean notExported);

    CompletionStage<api.v1.models.FactureWithDetails> serializeWithDetails(String organization, Bill facture);

    CompletionStage<Optional<String>> addAvoir(String organization, String factureUuid, CreditNote avoir);

    CompletionStage<Optional<Bill>> update(String organization, Bill bill, List<BillLine> lines);

    CompletionStage<Boolean> setExported(String organization, Date date);

    CompletionStage<List<BillComment>> getComments(String organization, String uuid);

    CompletionStage<Optional<BillComment>> addComment(String organization, BillComment billComment);

    CompletionStage<Optional<api.v1.models.BillComment>> serializeComment(String organization, BillComment comment);

    CompletionStage<File> generatePdfBill(String organization, String billId);

    CompletionStage<PaginatedResult<List<IndexableBill>>> getOverviews(final String organization, Pageable pageable);

    CompletionStage<Boolean> reindex(final String organization);
}
