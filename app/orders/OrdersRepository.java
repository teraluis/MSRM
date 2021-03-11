package orders;

import java.util.List;
import java.util.Optional;

public interface OrdersRepository {

    List<ReportDestination> getReportDestinationsFromEstablishment(String organization, String establishmentId);

    List<ReportDestination> getReportDestinationsFromMarket(String organization, String marketId);

    List<ReportDestination> getReportDestinationsFromOrder(String organization, String orderId);

    Optional<String> add(String organization, Order order);

    List<Order> getAll(String organization);

    List<Order> getFromList(String organization, List<String> orders);

    List<Order> getPage(String organization, Integer offset, Integer length);

    List<Order> search(String organization, String pattern);

    List<Order> searchPage(String organization, String pattern, Integer offset, Integer length);

    List<Order> searchPageFromStatus(String organization, OrderStatus status, String pattern, Integer offset, Integer length);

    Optional<Order> get(String organization, String uuid);

    List<Order> getFromMarket(String organization, String marketUuid);

    List<Order> getFromEstimate(String organization, String estimateUuid);

    List<Order> getFromEstablishment(String organization, String establishmentId);

    List<Order> getFromAccount(String organization, String accountId);

    Optional<Order> getFromBill(String organization, String billUuid);
    Boolean deleteReportDestination(final String o, String rp);
    Optional<String> addReportDestination(final String o, ReportDestination i);
    Boolean updateReportDestination(final String o, ReportDestination i);

    List<Order> getFromStatus(String organization, OrderStatus status);

    List<Order> getPageFromStatus(String organization, OrderStatus status, Integer offset, Integer length);

    List<OrderLine> getOrderLines(String organization, String orderUuid);

    List<String> getEstablishmentsWithOrder(final String organization);

    Boolean setStatus(String organization, String uuid, OrderStatus status);

    Optional<Order> checkName(String organization, String name);

    Optional<Order> update(String organization, Order order);

    Optional<String> delete(String organization, String uuid);

    Boolean setOrderLines(String organization, String orderUuid, List<OrderLine> orderLines, List<String> orderLinesToDelete);

    List<OrderComment> getComments(final String organization, final String uuid);

    Optional<OrderComment> addComment(final String organization, OrderComment comment);

}
