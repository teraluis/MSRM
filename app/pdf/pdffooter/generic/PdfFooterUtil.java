package pdf.pdffooter.generic;

import api.v1.models.FactureLigne;
import pdf.utils.CurrencyCalculation;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

import static pdf.utils.CurrencyCalculation.divide;
import static pdf.utils.CurrencyCalculation.multiply;
import static pdf.utils.CurrencyCalculation.subtract;

public class PdfFooterUtil {
    public static BigDecimal getTotalWithoutTax(List<FactureLigne> billLines, String taxPercent) {
        List<FactureLigne> billLineByTax = billLines.stream().filter(l -> taxPercent.equalsIgnoreCase(l.tvacode)).collect(Collectors.toList());
        return billLineByTax
                .stream()
                .reduce(BigDecimal.ZERO, (acc, cur) -> {
                    if (cur.discount.compareTo(BigDecimal.ZERO) > 0) {
                        return acc.add(subtract(
                                cur.price,
                                multiply(
                                        cur.price,
                                        divide(
                                                cur.discount,
                                                new BigDecimal(100)
                                        )
                                )
                        ));
                    } else {
                        return CurrencyCalculation.add(acc, cur.price);
                    }
                }, BigDecimal::add).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal getTotalWithTax(BigDecimal withoutTax, String tax) {
        return multiply(withoutTax, new BigDecimal(tax));
    }
}
