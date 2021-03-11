package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import core.models.People;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Order {

    public final String uuid;
    public final String name;
    public final Account account;
    public final String status;
    public final Long created;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<FullMarket> market;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Estimate> estimate;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> referenceNumber;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> referenceFile;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> received;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> deadline;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> adviceVisit;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> assessment;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> workdescription;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<People> purchaserContact;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<User> commercial;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<FullEstablishment> establishment;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> commentary;
    public final List<OrderLine> orderLines;
    public final List<ReportDestination> reportDestinations;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Agency> agency;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Establishment> billedEstablishment;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<People> billedContact;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Establishment> payerEstablishment;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<People> payerContact;

    public Order(final String uuid, final String name, final Account account, final String status, Long created, final Optional<FullMarket> market,
                 final Optional<Estimate> estimate, final Optional<String> referenceNumber, final Optional<String> referenceFile,
                 final Optional<Date> received, final Optional<Date> deadline, final Optional<Date> adviceVisit,
                 final Optional<Date> assessment, final Optional<String> description, final Optional<String> workdescription,
                 final Optional<People> purchaserContact, final Optional<User> commercial, final Optional<FullEstablishment> establishment,
                 final Optional<String> commentary, final List<OrderLine> orderLines, final List<ReportDestination> reportDestinations,
                 final Optional<Agency> agency, final Optional<Establishment> billedEstablishment, final Optional<People> billedContact,
                 Optional<Establishment> payerEstablishment, Optional<People> payerContact) {
        this.uuid = uuid;
        this.name = name;
        this.account = account;
        this.status = status;
        this.created = created;
        this.market = market;
        this.estimate = estimate;
        this.referenceNumber = referenceNumber;
        this.referenceFile = referenceFile;
        this.received = received;
        this.deadline = deadline;
        this.adviceVisit = adviceVisit;
        this.assessment = assessment;
        this.description = description;
        this.workdescription = workdescription;
        this.purchaserContact = purchaserContact;
        this.commercial = commercial;
        this.establishment = establishment;
        this.commentary = commentary;
        this.orderLines = orderLines;
        this.reportDestinations = reportDestinations;
        this.agency = agency;
        this.billedEstablishment = billedEstablishment;
        this.billedContact = billedContact;
        this.payerEstablishment = payerEstablishment;
        this.payerContact = payerContact;
    }

    public static Order serialize(orders.Order order, Account account, Optional<FullMarket> optMarket,
                                  Optional<Estimate> estimate, Optional<FullEstablishment> optEstablishment, Optional<People> purchaserContact,
                                  Optional<User> commercial, List<OrderLine> orderLines, List<ReportDestination> reportDestinations,
                                  Optional<Agency> agency, Optional<Establishment> billedEstablishment, Optional<People> billedContact,
                                  Optional<Establishment> payerEstablishment, Optional<People> payerContact) {
        return new Order(
                order.uuid,
                order.name,
                account,
                order.status.getId(),
                order.created.getTime(),
                optMarket,
                estimate,
                order.referenceNumber,
                order.referenceFile,
                order.received,
                order.deadline,
                order.adviceVisit,
                order.assessment,
                order.description,
                order.workdescription,
                purchaserContact,
                commercial,
                optEstablishment,
                order.commentary,
                orderLines,
                reportDestinations,
                agency,
                billedEstablishment,
                billedContact,
                payerEstablishment,
                payerContact);
    }
}
