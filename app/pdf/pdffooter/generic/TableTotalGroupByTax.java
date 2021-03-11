package pdf.pdffooter.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.utils.PdfUtilities;

import java.math.BigDecimal;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;
import static pdf.pdffooter.generic.PdfFooterUtil.getTotalWithTax;
import static pdf.pdffooter.generic.PdfFooterUtil.getTotalWithoutTax;
import static pdf.utils.PdfUtilities.df;
import static pdf.utils.PdfUtilities.setTextToRight;

public class TableTotalGroupByTax {

    public static PdfPTable totalByTax(FactureWithDetails bill) {
        PdfPTable wrapper = new PdfPTable(1);

        PdfPTable totalTableWrapper = new PdfPTable(1);
        PdfPTable totalTable = new PdfPTable(3);

        totalTable.addCell(new PdfPCell(new Phrase("Taux TVA", DEFAULT_FONT)));
        totalTable.addCell(new PdfPCell(new Phrase("Base HT", DEFAULT_FONT)));
        totalTable.addCell(new PdfPCell(new Phrase("Montant TVA", DEFAULT_FONT)));

        BigDecimal totalWithoutTax = getTotalWithoutTax(bill.bill.lignes, "20%");

        totalTable.addCell(new PdfPCell(new Phrase("20%", DEFAULT_FONT)));
        totalTable.addCell(setTextToRight(df.format(totalWithoutTax)));
        totalTable.addCell(setTextToRight(df.format(getTotalWithTax(totalWithoutTax, "1.2"))));

        totalWithoutTax = getTotalWithoutTax(bill.bill.lignes, "10%");

        totalTable.addCell(new PdfPCell(new Phrase("10%", DEFAULT_FONT)));
        totalTable.addCell(setTextToRight(df.format(totalWithoutTax)));
        totalTable.addCell(setTextToRight(df.format(getTotalWithTax(totalWithoutTax, "1.1"))));

        totalWithoutTax = getTotalWithoutTax(bill.bill.lignes, "0%");

        totalTable.addCell(new PdfPCell(new Phrase("0%", DEFAULT_FONT)));
        totalTable.addCell(setTextToRight(df.format(totalWithoutTax)));
        totalTable.addCell(setTextToRight(df.format(totalWithoutTax)));

        PdfUtilities.addCell(totalTableWrapper, totalTable);
        PdfUtilities.addCell(totalTableWrapper, PdfUtilities.invisibleCell());
        PdfUtilities.addCell(wrapper, totalTableWrapper);

        return wrapper;
    }
}
