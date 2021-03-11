package bills;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CreditNote {

    public final String uuid;
    public final String name;
    public final Date date;
    public final Optional<Date> exportdate;

    public CreditNote(final Optional<String> uuid, String name, Date date, Optional<Date> exportdate) {
        this.uuid = uuid.orElseGet(() -> "creditnote-" + UUID.randomUUID());
        this.name = name;
        this.date = date;
        this.exportdate = exportdate;
    }
}
