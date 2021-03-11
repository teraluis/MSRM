package orders;

import core.search.Pageable;
import missionclient.PrestationWithEstate;
import scala.Tuple2;
import core.search.PaginatedResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface OrdersService {

    CompletionStage<List<ReportDestination>> getReportDestinationsFromOrder(String organization, String orderUuid, Optional<String> estimateId, Optional<String> marketId, Optional<String> establishmentId);

    CompletionStage<Optional<String>> add(String organization, Order order, Optional<String> login);

    CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> getPrestationsWithEstate(String organization);

    CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Order>> getAll(String organization);

    CompletionStage<List<Order>> search(String organization, String pattern);

    CompletionStage<List<Tuple2<Order, List<PrestationWithEstate>>>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<Order>> get(String organization, String uuid);

    CompletionStage<List<Order>> getFromMarket(String organization, String marketUuid);

    CompletionStage<List<Order>> getFromEstimate(String organization, String estimateUuid);

    CompletionStage<List<Order>> getFromEstate(String organization, String estateId);

    CompletionStage<List<Order>> getFromEstablishment(String organization, String establishmentId);

    CompletionStage<List<Order>> getFromAccount(String organization, String accountId);

    CompletionStage<Optional<Order>> getFromBill(String organization, String billUuid);

    CompletionStage<List<OrderLine>> getOrderLines(String organization, String orderUuid);

    CompletionStage<Optional<Order>> checkName(String organization, String name);

    CompletionStage<Boolean> setStatus(String organization, String uuid, OrderStatus status, Optional<String> login);

    CompletionStage<Optional<api.v1.models.Order>> serializeOrder(String organization, Order order);

    CompletionStage<Optional<Order>> update(String organization, Order order, Optional<String> login);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<Boolean> setOrderLines(String organization, String orderUuid, List<OrderLine> orderLines, List<String> orderLinesToDelete);

    CompletionStage<IndexableOrder> buildIndexableOrder(final String organization, final Order order);

    CompletionStage<Boolean> reindex(final String organization);

    CompletionStage<PaginatedResult<List<IndexableOrder>>> getOverviews(final String organization, Pageable pageable);

    CompletionStage<List<String>> getEstablishmentsWithOrder(final String organization);

    CompletionStage<List<OrderComment>> getComments(final String organization, final String uuid);

    CompletionStage<Optional<OrderComment>> addComment(final String organization, OrderComment comment);

    CompletionStage<Optional<api.v1.models.OrderComment>> serializeComment(final String organization, OrderComment comment);
    CompletionStage<Optional<String>> addReportDestination(final String o, ReportDestination rp);
    CompletionStage<Boolean> updateReportDestination(final String o, ReportDestination rp);
    CompletionStage<Boolean> deleteReportDestination(final String o, String i);
}
