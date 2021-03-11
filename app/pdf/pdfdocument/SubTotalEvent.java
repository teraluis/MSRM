package pdf.pdfdocument;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import java.math.BigDecimal;

public class SubTotalEvent implements PdfPCellEvent {

    BigDecimal price;
    Totals totals;

    public SubTotalEvent(Totals totals, BigDecimal price) {
        this.totals = totals;
        this.price = price;
    }

    public SubTotalEvent(Totals totals) {
        this.totals = totals;
        price = BigDecimal.ZERO;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        if (price == null) {
            PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
            ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                    new Phrase(String.valueOf(totals.subtotal), new Font(Font.FontFamily.UNDEFINED, 6)),
                    position.getRight() - 2, position.getBottom() + 2, 0);
            totals.lastSubTotal = totals.subtotal;
            totals.subtotal = BigDecimal.ZERO;
            return;
        }
        totals.subtotal = totals.subtotal.add(price);
        totals.total = totals.total.add(price);
    }

}
