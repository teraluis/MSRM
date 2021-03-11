package pdf.pdfdocument;

import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.*;

import java.math.BigDecimal;

public class PreviousSubTotalEvent implements PdfPCellEvent {

    BigDecimal price;
    Totals totals;

    public PreviousSubTotalEvent(Totals totals, BigDecimal price) {
        this.totals = totals;
        this.price = price;
    }

    public PreviousSubTotalEvent(Totals totals) {
        this.totals = totals;
        price = BigDecimal.ZERO;
    }

    @Override
    public void cellLayout(PdfPCell cell, Rectangle position, PdfContentByte[] canvases) {
        PdfContentByte canvas = canvases[PdfPTable.TEXTCANVAS];
        ColumnText.showTextAligned(canvas, Element.ALIGN_RIGHT,
                new Phrase(String.valueOf(totals.lastSubTotal), new Font(Font.FontFamily.UNDEFINED, 6)),
                position.getRight() - 2, position.getBottom() + 2, 0);
        totals.subtotal = totals.subtotal.add(totals.lastSubTotal);
    }

}
