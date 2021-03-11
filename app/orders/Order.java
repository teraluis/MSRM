package orders;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Order implements Single<String> {

    public final String uuid;
    public final String name;
    public final String account;
    public final OrderStatus status;
    public final Date created;
    public final Optional<String> market;
    public final Optional<String> estimate;
    public final Optional<String> referenceNumber;
    public final Optional<String> referenceFile;
    public final Optional<Date> received;
    public final Optional<Date> deadline;
    public final Optional<Date> adviceVisit;
    public final Optional<Date> assessment;
    public final Optional<String> description;
    public final Optional<String> workdescription;
    public final Optional<String> purchaserContact;
    public final Optional<String> commercial;
    public final Optional<String> establishment;
    public final Optional<String> commentary;
    public final Optional<String> agency;
    public final Optional<String> billedEstablishment;
    public final Optional<String> billedContact;
    public final Optional<String> payerEstablishment;
    public final Optional<String> payerContact;

    public Order(final Optional<String> uuid, final String name, final String account, final OrderStatus status, Date created, final Optional<String> market,
                 final Optional<String> estimate, final Optional<String> referenceNumber, final Optional<String> referenceFile,
                 final Optional<Date> received, final Optional<Date> deadline, final Optional<Date> adviceVisit, final Optional<Date> assessment,
                 final Optional<String> description, final Optional<String> workdescription, final Optional<String> purchaserContact,
                 Optional<String> commercial, final Optional<String> establishment, final Optional<String> commentary,
                 final Optional<String> agency, final Optional<String> billedEstablishment, final Optional<String> billedContact,
                 Optional<String> payerEstablishment, Optional<String> payerContact) {
        this.uuid = uuid.orElseGet(() -> "order-" + UUID.randomUUID());
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
        this.agency = agency;
        this.billedEstablishment = billedEstablishment;
        this.billedContact = billedContact;
        this.payerEstablishment = payerEstablishment;
        this.payerContact = payerContact;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
