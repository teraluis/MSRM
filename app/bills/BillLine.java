package bills;

import core.Single;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class BillLine implements Single<String> {

    public final String uuid;
    public final String refadx;
    public final Optional<String> refbpu;
    public final Optional<String> designation;
    public final String tvacode;
    public final BigDecimal price;
    public final Integer quantity;
    public final BigDecimal total;
    public final BigDecimal discount;
    public final Date billingdate;
    public final Optional<String> creditnote;

    public BillLine(final Optional<String> uuid, final String refadx, final Optional<String> refbpu, final Optional<String> designation, final String tvacode, final BigDecimal price, final Integer quantity, final BigDecimal total, final BigDecimal discount, final Date billingdate, final Optional<String> creditnote) {
        this.uuid = uuid.orElseGet(() -> "billline-" + UUID.randomUUID());
        this.refadx = refadx;
        this.refbpu = refbpu;
        this.designation = designation;
        this.tvacode = tvacode;
        this.price = price;
        this.quantity = quantity;
        this.total = total;
        this.discount = discount;
        this.billingdate = billingdate;
        this.creditnote = creditnote;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
