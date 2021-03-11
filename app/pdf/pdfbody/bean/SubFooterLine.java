package pdf.pdfbody.bean;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.pdfdocument.SubTotalEvent;
import pdf.pdfdocument.Totals;

public class SubFooterLine {
    public SubFooterLine(PdfPTable table, int startCell, String label, Totals totals) throws AssertionError {
        if (startCell > table.getNumberOfColumns()) {
            throw new AssertionError("startCell out of table bound");
        }

        if (startCell > 0) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(0);
            cell.setBorderWidthTop(0.5f);
            cell.setColspan(startCell - 1);
            table.addCell(cell);
        }

        PdfPCell labelCell = new PdfPCell(new Phrase(label, new Font(Font.FontFamily.UNDEFINED, 6)));
        labelCell.setColspan(table.getNumberOfColumns() - startCell);

        labelCell.setBorder(0);
        labelCell.setBorderWidthLeft(0.5f);
        labelCell.setBorderWidthBottom(0.5f);
        labelCell.setBorderWidthTop(0.5f);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell();
        valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valueCell.setBorder(0);
        valueCell.setBorderWidthRight(0.5f);
        valueCell.setBorderWidthBottom(0.5f);
        valueCell.setBorderWidthLeft(0.5f);
        valueCell.setBorderWidthTop(0.5f);
        valueCell.setCellEvent(new SubTotalEvent(totals));
        table.addCell(valueCell);
    }
}
