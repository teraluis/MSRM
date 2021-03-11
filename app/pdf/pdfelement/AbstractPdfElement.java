package pdf.pdfelement;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;

import java.io.IOException;
import java.util.List;

public abstract class AbstractPdfElement {
    protected Document document;

    public AbstractPdfElement(Document document) {
        this.document = document;
    }

    public abstract List<Element> generateContent() throws IOException, DocumentException;
}
