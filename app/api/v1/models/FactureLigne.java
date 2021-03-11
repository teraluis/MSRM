package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import bills.BillLine;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public class FactureLigne {

    public final String uuid;
    public final String refadx;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> refbpu;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> designation;
    public final String tvacode;
    public final BigDecimal price;
    public final Integer quantity;
    public final BigDecimal total;
    public final BigDecimal discount;
    public final Date billingdate;
    public final String expertCode;

    public FactureLigne(final String uuid, final String refadx, Optional<String> refbpu, Optional<String> designation, final String tvacode, final BigDecimal total, final BigDecimal discount, final Date billingdate, final BigDecimal price, Integer quantity, String expertCode) {
        this.uuid = uuid;
        this.refadx = refadx;
        this.refbpu = refbpu;
        this.designation = designation;
        this.tvacode = tvacode;
        this.total = total;
        this.discount = discount;
        this.billingdate = billingdate;
        this.price = price;
        this.quantity = quantity;
        this.expertCode = expertCode;
    }

    public static FactureLigne serialize(BillLine billLine, String expertCode) {
        return new FactureLigne(billLine.uuid, billLine.refadx, billLine.refbpu, billLine.designation, billLine.tvacode, billLine.total, billLine.discount, billLine.billingdate, billLine.price, billLine.quantity, expertCode);
    }
}
