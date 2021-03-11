package pdf.pdfheader.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import missionclient.interventions.DoneIntervention;
import pdf.utils.PdfUtilities;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;
import static pdf.utils.PdfUtilities.formatDate;

public class HeaderPdfBillIdentificationBlock {

    private HeaderPdfBillIdentificationBlock() {
    }

    public static PdfPTable basicIdBillBlock(FactureWithDetails bill) {
        PdfPTable wrapper = new PdfPTable(2);

        Phrase billPhrase = new Phrase();
        Font billIdFont = new Font(DEFAULT_FONT);
        billIdFont.setSize(12);
        billIdFont.setStyle(Font.BOLD);
        Chunk billLabel = new Chunk("Facture N°", billIdFont);
//        billLabel.setUnderline(0.1f, -2f);
        billPhrase.add(billLabel);
        billPhrase.add(new Chunk(" : ", billIdFont));
        billPhrase.add(new Chunk(bill.bill.name, billIdFont));

        PdfPCell subCell = new PdfPCell(billPhrase);
        subCell.setBorder(0);
        subCell.setColspan(2);
        subCell.setUseAscender(true);
        subCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        subCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        subCell.setBackgroundColor(PdfUtilities.computeRGBColorFromHexCode("#cacbd3"));
        wrapper.addCell(subCell);

        List<DoneIntervention> interventions = bill.interventions.stream().map(i -> (DoneIntervention) i).collect(Collectors.toList());
        Optional<Date> startDate = interventions.stream().map(i -> i.getParameters().getInterventionDate()).min(Date::compareTo);
        Optional<Date> endDate = interventions.stream().map(i -> i.getParameters().getInterventionDate()).max(Date::compareTo);

        if (startDate.isPresent() && endDate.isPresent()) {
            Phrase periodDatePhrase = new Phrase();
            Chunk periodDateLabel = new Chunk("Période", DEFAULT_FONT);
            periodDateLabel.setUnderline(0.1f, -2f);
            periodDatePhrase.add(periodDateLabel);
            periodDatePhrase.add(new Chunk(" : ", DEFAULT_FONT));
            periodDatePhrase.add(new Chunk(String.format("du %s au %s", formatDate(startDate.get()), formatDate(endDate.get())), DEFAULT_FONT));

            subCell = new PdfPCell(periodDatePhrase);
            subCell.setColspan(2);
            subCell.setBorder(0);
            wrapper.addCell(subCell);
        }

        Phrase issueDatePhrase = new Phrase();
        Chunk issueDateLabel = new Chunk("Date émission", DEFAULT_FONT);
        issueDateLabel.setUnderline(0.1f, -2f);
        issueDatePhrase.add(issueDateLabel);
        issueDatePhrase.add(new Chunk(" : ", DEFAULT_FONT));
        issueDatePhrase.add(new Chunk(formatDate(new Date()), DEFAULT_FONT));
        PdfUtilities.addCell(wrapper, issueDatePhrase);

        Phrase currencyPhrase = new Phrase();
        Chunk currencyabel = new Chunk("Devise", DEFAULT_FONT);
        currencyabel.setUnderline(0.1f, -2f);
        currencyPhrase.add(currencyabel);
        currencyPhrase.add(new Chunk(" : EUR", DEFAULT_FONT));
        PdfUtilities.addCell(wrapper, currencyPhrase);

        return wrapper;
    }
}
