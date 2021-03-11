package pdf.utils;

import com.itextpdf.text.pdf.PdfContentByte;

public class SolidBorder extends CustomBorder {
    public SolidBorder(int border) { super(border); }
    public void setLineDash(PdfContentByte canvas) {}
}
