package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Optional;

public class OrderLine {

    public final String uuid;
    public final String refadx;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> refbpu;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> designation;
    public final BigDecimal price;
    public final Integer quantity;
    public final Float discount;
    public final String tvacode;
    public final Float total;

    public OrderLine(final String uuid, final String refadx, final Optional<String> refbpu, final Optional<String> designation, final BigDecimal price, final Integer quantity, final Float discount, final String tvacode, final Float total) {
        this.uuid = uuid;
        this.refadx = refadx;
        this.refbpu = refbpu;
        this.designation = designation;
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.tvacode = tvacode;
        this.total = total;
    }

    public OrderLine(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("refadx") String refadx,
            @JsonProperty("refbpu") String refbpu,
            @JsonProperty("designation") String designation,
            @JsonProperty("price") BigDecimal price,
            @JsonProperty("quantity") Integer quantity,
            @JsonProperty("discount") Float discount,
            @JsonProperty("tvacode") String tvacode,
            @JsonProperty("total") Float total) {
        this.uuid = uuid;
        this.refadx = refadx;
        this.refbpu = Optional.ofNullable(refbpu);
        this.designation = Optional.ofNullable(designation);
        this.price = price;
        this.quantity = quantity;
        this.discount = discount;
        this.tvacode = tvacode;
        this.total = total;
    }

    public static OrderLine serialize(orders.OrderLine orderLine) {
        return new OrderLine(orderLine.uuid, orderLine.refadx, orderLine.refbpu, orderLine.designation, orderLine.price, orderLine.quantity, orderLine.discount, orderLine.tvacode, orderLine.total);
    }
}
