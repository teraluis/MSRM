package pdf.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CurrencyCalculation {

    private CurrencyCalculation() {
    }

    public static BigDecimal multiply(BigDecimal num1, BigDecimal num2) {
        return num1.setScale(4, RoundingMode.HALF_UP).multiply(num2.setScale(4, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal divide(BigDecimal num1, BigDecimal num2) {
        return num1.setScale(4, RoundingMode.HALF_UP).divide(num2.setScale(4, RoundingMode.HALF_UP), RoundingMode.HALF_UP).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal add(BigDecimal num1, BigDecimal num2) {
        return num1.setScale(4, RoundingMode.HALF_UP).add(num2.setScale(4, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal subtract(BigDecimal num1, BigDecimal num2) {
        return num1.setScale(4, RoundingMode.HALF_UP).subtract(num2.setScale(4, RoundingMode.HALF_UP)).setScale(4, RoundingMode.HALF_UP);
    }
}
