package utils.Sage1000Export;

import bills.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PaymentParsingFile {
    protected final static Logger logger = LoggerFactory.getLogger(PaymentParsingFile.class);

    public static Map<String, List<Payment>> parsePaymentFile(List<List<String>> file) {
        Map<String, List<Payment>> payments = new HashMap<>();
        for (List<String> line : file) {
            if (line.size() > 6) {
                if (!payments.containsKey(line.get(1))) {
                    payments.put(line.get(1), new ArrayList<>());
                }
                payments.get(line.get(1)).add(new Payment(Optional.empty(), "VIR", new BigDecimal(line.get(6).replaceAll(",", ".")), true, new Date(), Optional.of(new Date())));
            }
        }
        return payments;
    }
}
