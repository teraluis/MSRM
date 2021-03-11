package orders;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public class OrderLine {

    public final String uuid;
    public final String refadx;
    public final Optional<String> refbpu;
    public final Optional<String> designation;
    public final BigDecimal price;
    public final Integer quantity;
    public final Float discount;
    public final String tvacode;
    public final Float total;

    public OrderLine(final Optional<String> uuid, final String refadx, final Optional<String> refbpu, final Optional<String> designation, final BigDecimal price, final Integer quantity, final Float discount, final String tvacode, final Float total) {
        this.uuid = uuid.orElseGet(() -> "orderline-" + UUID.randomUUID());
        this.refadx = refadx;
        this.refbpu = refbpu;
        this.designation = designation;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.tvacode = tvacode;
        this.total = total;
    }
}
