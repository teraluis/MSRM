package pdf.pdffooter;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import missionclient.interventions.DoneIntervention;
import pdf.pdfelement.AbstractPdfElement;
import pdf.pdffooter.generic.FooterPdfDetachablePart;
import pdf.pdffooter.generic.FooterPdfInformationsPart;
import pdf.pdffooter.generic.FooterPdfNotePart;
import pdf.pdffooter.generic.TableTotalGroupByTax;
import pdf.pdffooter.generic.TableTotalWithTax;
import pdf.utils.PdfUtilities;

import java.util.ArrayList;
import java.util.List;

import static pdf.utils.PdfUtilities.invisibleCell;

public class BasicBillFooter extends AbstractPdfElement {
    FactureWithDetails bill;
    DoneIntervention intervention;

    public BasicBillFooter(Document document, FactureWithDetails bill, DoneIntervention intervention) {
        super(document);
        this.bill = bill;
        this.intervention = intervention;
    }

    @Override
    public List<Element> generateContent() {
        List<Element> elements = new ArrayList<>();
        float[] colsWidth = {6f, 0.5f, 3f};
        PdfPTable footerTable = new PdfPTable(colsWidth);
        footerTable.setTotalWidth(document.right(document.rightMargin()) - document.left(document.leftMargin()) - 70);

        PdfPTable subTable = new PdfPTable(2);

        PdfUtilities.addCell(subTable, FooterPdfNotePart.notePart(bill));
        PdfUtilities.addCell(subTable, TableTotalGroupByTax.totalByTax(bill));

        PdfPTable spacingTable = new PdfPTable(1);
        PdfUtilities.addCell(spacingTable, TableTotalWithTax.totalWithTax(bill));
        PdfUtilities.addCell(spacingTable, invisibleCell());

        PdfUtilities.addCell(footerTable, subTable);
        PdfUtilities.addCell(footerTable, invisibleCell());
        PdfUtilities.addCell(footerTable, spacingTable);

        PdfUtilities.addCell(footerTable, new PdfPCell(FooterPdfInformationsPart.informationsPart(bill, intervention)));
        PdfUtilities.addCell(footerTable, invisibleCell());
        PdfUtilities.addCell(footerTable, FooterPdfDetachablePart.detachablePart(bill, intervention));

        elements.add(footerTable);
        return elements;
    }
}
