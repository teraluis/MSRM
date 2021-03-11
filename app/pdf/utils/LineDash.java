package pdf.utils;

import com.itextpdf.text.pdf.PdfContentByte;

public interface LineDash {
    void applyLineDash(PdfContentByte canvas);
}
