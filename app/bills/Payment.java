package bills;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Payment {

    public final String uuid;
    public final String type;
    public final BigDecimal value;
    public final Boolean received;
    public final Date date;
    public final Optional<Date> exportdate;

    public Payment(final Optional<String> uuid, String type, BigDecimal value, Boolean received, Date date, Optional<Date> exportdate) {
        this.uuid = uuid.orElseGet(() -> "payment-" + UUID.randomUUID());
        this.type = type;
        this.value = value;
        this.received = received;
        this.date = date;
        this.exportdate = exportdate;
    }

}
