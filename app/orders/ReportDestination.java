package orders;

import java.util.Optional;
import java.util.UUID;

public class ReportDestination {

    public final String uuid;
    public final Optional<String> order;
    public final Optional<String> mail;
    public final Optional<String> url;
    public final Optional<String> address;
    public final Optional<String> people;
    public final Optional<String> establishment;

    public ReportDestination(final Optional<String> uuid, final Optional<String> order, final Optional<String> mail, final Optional<String> url,
                             final Optional<String> address, final Optional<String> people, final Optional<String> establishment) {
        this.uuid = uuid.orElseGet(() -> "reportdestination-" + UUID.randomUUID());
        this.order = order;
        this.mail = mail;
        this.url = url;
        this.address = address;
        this.people = people;
        this.establishment = establishment;
    }
}
