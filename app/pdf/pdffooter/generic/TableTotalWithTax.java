package pdf.pdffooter.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPTable;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;
import static pdf.utils.BillUtilities.getTotalWithTaxes;
import static pdf.utils.BillUtilities.getTotalWithoutTaxes;
import static pdf.utils.CurrencyCalculation.subtract;
import static pdf.utils.PdfUtilities.df;
import static pdf.utils.PdfUtilities.setTextToRight;

public class TableTotalWithTax {
    public static PdfPTable totalWithTax(FactureWithDetails bill) {
        PdfPTable wrapper = new PdfPTable(new float[]{1.5f, 0.5f});

        wrapper.addCell(new Phrase("Montant Total HT", DEFAULT_FONT));
        wrapper.addCell(setTextToRight(
                df.format(getTotalWithoutTaxes(bill))
        ));

        wrapper.addCell(new Phrase("Montant Total TVA", DEFAULT_FONT));

        wrapper.addCell(setTextToRight(
                df.format(subtract(getTotalWithTaxes(bill), getTotalWithoutTaxes(bill)))
        ));

        wrapper.addCell(new Phrase("Montant Total NET", DEFAULT_FONT));
        wrapper.addCell(setTextToRight(
                df.format(getTotalWithTaxes(bill))
        ));

        return wrapper;
    }
}
