package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import core.models.Address;
import core.models.People;

import java.util.Optional;

public class ReportDestination {

    public final String uuid;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> order;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> mail;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> url;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Address> address;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<People> people;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Establishment> establishment;

    public ReportDestination(String uuid, Optional<String> order, Optional<String> mail, Optional<String> url,
                             Optional<Address> address, Optional<People> people, Optional<Establishment> establishment) {
        this.uuid = uuid;
        this.order = order;
        this.mail = mail;
        this.url = url;
        this.address = address;
        this.people = people;
        this.establishment = establishment;
    }

    public static ReportDestination serialize(orders.ReportDestination reportDestination, Optional<Address> address, Optional<People> people, Optional<Establishment> establishment) {
        return new ReportDestination(reportDestination.uuid, reportDestination.order, reportDestination.mail, reportDestination.url, address, people, establishment);
    }
}
