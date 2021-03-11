package pdf.pdfdocument;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdf.pdfelement.AbstractPdfElement;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

public abstract class AbstractPdfDocument extends Document {

    private final Logger log = LoggerFactory.getLogger(AbstractPdfDocument.class);

    protected AbstractPdfDocument() {
        this.setMargins(0, 0, 40, 60);
    }

    protected abstract AbstractPdfElement getHeader();

    protected abstract AbstractPdfElement getBody();

    protected abstract AbstractPdfElement getFooter();

    public File generatePdfDocument(String name) throws IOException {
        ByteArrayOutputStream file = new ByteArrayOutputStream();
        generatePdfDocument(file);
        file.close();
        return new File(file, name);
    }

    public void generatePdfDocument(OutputStream out) {
        try {
            PdfWriter writer = PdfWriter.getInstance(this, out);

            BasicBillEvent event = new BasicBillEvent();
            writer.setPageEvent(event);

            this.open();

            List<Element> header = getHeader().generateContent();
            if (!header.isEmpty()) {
                this.add(header.get(0));
            }

            PdfContentByte canvas = writer.getDirectContent();
            canvas.setColorStroke(BaseColor.BLACK);
            canvas.moveTo(36, 595);
            canvas.lineTo(559, 595);
            canvas.closePathStroke();

            List<Element> body = getBody().generateContent();
            if (!body.isEmpty()) {
                this.add(body.get(0));
            }

            List<Element> footer = getFooter().generateContent();
            if (!footer.isEmpty()) {
                PdfPTable table = (PdfPTable) footer.get(0);
                table.writeSelectedRows(0, -1,
                        35,
                        table.getTotalHeight() + 50,
                        writer.getDirectContent());
            }
        } catch (DocumentException | IOException e) {
            log.error("PDF generation error", e);
        }
        this.close();

    }
}
