package pdf.pdfbody.generic;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.pdfbody.bean.TableHeader;

import java.util.List;

public class BodyPdfTableHeader {

    public static void arrayHeader(PdfPTable table, List<TableHeader> headers) {
        for ( int i = 0; i < headers.size(); i++) {
            TableHeader header = headers.get(i);

            PdfPCell cell = new PdfPCell(new Phrase(header.getName(), new Font(Font.FontFamily.UNDEFINED, 6, Font.BOLD, BaseColor.BLACK)));
            cell.setColspan(header.getNbColumn());
            cell.setBorder(0);
            cell.setBorderWidthLeft(0.5f);
            cell.setBorderWidthTop(0.5f);
            cell.setBorderWidthBottom(0.5f);
            if ( i == headers.size() - 1 ) { cell.setBorderWidthRight(0.5f); }

            table.addCell(cell);
        }
    }
}
