package pdf.pdfheader;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.utils.PdfUtilities;
import pdf.pdfelement.AbstractPdfElement;
import pdf.pdfheader.generic.HeaderPdfAdxInfoBlock;
import pdf.pdfheader.generic.HeaderPdfBillIdentificationBlock;
import pdf.pdfheader.generic.HeaderPdfClientInfoBlock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BasicBillHeader extends AbstractPdfElement {
    FactureWithDetails bill;
    public BasicBillHeader(Document document, FactureWithDetails bill) {
        super(document);
        this.bill = bill;
    }

    @Override
    public List<Element> generateContent() throws IOException, DocumentException {
        List<Element> elements = new ArrayList<>();
        float[] colsWidth = {5, 1, 5};
        PdfPTable headerTable = new PdfPTable(colsWidth);

        PdfPTable leftColumn = new PdfPTable(1);
        PdfPTable rightColumn = new PdfPTable(1);

        PdfUtilities.addCell(leftColumn, PdfUtilities.createImageCell("conf/img/logo_adx.png"));
        PdfPCell spacing = new PdfPCell();
        spacing.setBorder(0);
        spacing.setFixedHeight(10);
        PdfUtilities.addCell(leftColumn, spacing);
        PdfUtilities.addCell(leftColumn, HeaderPdfAdxInfoBlock.basicAdxInfo(this.bill));

        spacing.setFixedHeight(40);

//        PdfUtilities.addCell(rightColumn, HeaderPdfBillIdentificationBlock.basicIdBillBlock(this.bill));
        rightColumn.addCell(HeaderPdfBillIdentificationBlock.basicIdBillBlock(this.bill));
        PdfUtilities.addCell(rightColumn, spacing);
        PdfUtilities.addCell(rightColumn, HeaderPdfClientInfoBlock.basicClientBlock(this.bill));

        PdfUtilities.addCell(headerTable, leftColumn);
        PdfUtilities.addCell(headerTable, PdfUtilities.invisibleCell());
        PdfUtilities.addCell(headerTable, rightColumn);

        headerTable.setSpacingAfter(25);
        elements.add(headerTable);
        return elements;
    }
}
