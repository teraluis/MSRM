package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import bills.Payment;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Optional;

public class Paiement {

    public final String uuid;
    public final String type;
    public final BigDecimal value;
    public final Boolean received;
    public final Date date;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> exportDate;


    public Paiement(String uuid, String type, BigDecimal value, Boolean received, Date date, Optional<Date> exportDate) {
        this.uuid = uuid;
        this.type = type;
        this.value = value;
        this.received = received;
        this.date = date;
        this.exportDate = exportDate;
    }

    public static Paiement serialize(Payment payment) {
        return new Paiement(payment.uuid, payment.type, payment.value, payment.received, payment.date, payment.exportdate);
    }
}
