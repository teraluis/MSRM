package pdf.utils;

import api.v1.models.FactureWithDetails;
import api.v1.models.Paiement;

import java.math.BigDecimal;

import static pdf.pdffooter.generic.PdfFooterUtil.getTotalWithTax;
import static pdf.pdffooter.generic.PdfFooterUtil.getTotalWithoutTax;
import static pdf.utils.CurrencyCalculation.subtract;

public class BillUtilities {
    public static BigDecimal remainingBalance(FactureWithDetails bill) {
        BigDecimal advancePayment = bill
                .bill
                .paiements
                .stream()
                .reduce(BigDecimal.ZERO, (BigDecimal acc, Paiement cur) -> CurrencyCalculation.add(acc, cur.value), BigDecimal::add);
        return subtract(getTotalWithTaxes(bill), advancePayment);
    }

    public static BigDecimal getTotalWithTaxes(FactureWithDetails bill) {
        BigDecimal totalWithoutTaxFor20 = getTotalWithoutTax(bill.bill.lignes, "20%");
        BigDecimal totalWithoutTaxFor10 = getTotalWithoutTax(bill.bill.lignes, "10%");
        BigDecimal totalWithoutTaxFor0 = getTotalWithoutTax(bill.bill.lignes, "0%");

        BigDecimal totalWithTaxFor20 = getTotalWithTax(totalWithoutTaxFor20, "1.2");
        BigDecimal totalWithTaxFor10 = getTotalWithTax(totalWithoutTaxFor10, "1.1");

        return CurrencyCalculation.add(totalWithTaxFor20, CurrencyCalculation.add(totalWithTaxFor10, totalWithoutTaxFor0));
    }

    public static BigDecimal getTotalWithoutTaxes(FactureWithDetails bill) {
        BigDecimal totalWithoutTaxFor20 = getTotalWithoutTax(bill.bill.lignes, "20%");
        BigDecimal totalWithoutTaxFor10 = getTotalWithoutTax(bill.bill.lignes, "10%");
        BigDecimal totalWithoutTaxFor0 = getTotalWithoutTax(bill.bill.lignes, "0%");

        return CurrencyCalculation.add(totalWithoutTaxFor20, CurrencyCalculation.add(totalWithoutTaxFor10, totalWithoutTaxFor0));
    }
}
