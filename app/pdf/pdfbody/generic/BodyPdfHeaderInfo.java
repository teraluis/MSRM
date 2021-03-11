package pdf.pdfbody.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import missionclient.interventions.DoneIntervention;
import pdf.utils.PdfUtilities;
import pdf.utils.SearchProperty;

import java.util.Date;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;

public class BodyPdfHeaderInfo {

    public static PdfPTable arrayHeaderInfo(FactureWithDetails bill, DoneIntervention intervention, SearchProperty search) {
        int totalSizeUse = 0;
        PdfPTable t = new PdfPTable(4);
        t.setSpacingAfter(10);


        if (false) {
            totalSizeUse += 1;
            Phrase quotationRefPhrase = new Phrase();
            Chunk quotationRefLabel = new Chunk("Référence Devis :", DEFAULT_FONT);
            quotationRefLabel.setUnderline(0.1f, -2f);
            quotationRefPhrase.add(quotationRefLabel);
            quotationRefPhrase.add(new Chunk(" ", DEFAULT_FONT));
            quotationRefPhrase.add(new Chunk("NULL", DEFAULT_FONT));
            PdfPCell quotationRef = new PdfPCell(quotationRefPhrase);
            quotationRef.setBorder(0);
            t.addCell(quotationRef);
        }


        if (false) {
            totalSizeUse += 1;
            Phrase quotationDatePhrase = new Phrase();
            Chunk quotationDateLabel = new Chunk("Date Devis", DEFAULT_FONT);
            quotationDateLabel.setUnderline(0.1f, -2f);
            quotationDatePhrase.add(quotationDateLabel);
            quotationDatePhrase.add(new Chunk(" : ", DEFAULT_FONT));
            quotationDatePhrase.add(new Chunk("NULL", DEFAULT_FONT));
            PdfPCell quotationDate = new PdfPCell(quotationDatePhrase);
            quotationDate.setBorder(0);
            t.addCell(quotationDate);
        }

        totalSizeUse += 1;
        Phrase refOrderPhrase = new Phrase();
        Chunk refOrderLabel = new Chunk("Référence BDC", DEFAULT_FONT);
        refOrderLabel.setUnderline(0.1f, -2f);
        refOrderPhrase.add(refOrderLabel);
        refOrderPhrase.add(new Chunk(" : ", DEFAULT_FONT));
        refOrderPhrase.add(new Chunk(bill.order.name, DEFAULT_FONT));
        PdfPCell cRefOrderForm = new PdfPCell(refOrderPhrase);
        cRefOrderForm.setBorder(0);
        t.addCell(cRefOrderForm);

        totalSizeUse += 1;
        Phrase dateOrderPhrase = new Phrase();
        Chunk dateOrderLabel = new Chunk("Date BDC", DEFAULT_FONT);
        dateOrderLabel.setUnderline(0.1f, -2f);
        dateOrderPhrase.add(dateOrderLabel);
        dateOrderPhrase.add(new Chunk(" : ", DEFAULT_FONT));
        dateOrderPhrase.add(new Chunk(PdfUtilities.formatDate(new Date(bill.order.created)), DEFAULT_FONT));
        PdfPCell cDateOrderForm = new PdfPCell(dateOrderPhrase);
        cDateOrderForm.setBorder(0);
        t.addCell(cDateOrderForm);

        if (bill.order.establishment.isPresent()) {
            totalSizeUse += 2;
            PdfPTable sub = new PdfPTable(new float[]{0.58f, 1.5f});
            Paragraph manageUnitPhrase = new Paragraph();
            Chunk manageUnitLabel = new Chunk("Unité de gestion", DEFAULT_FONT);
            manageUnitLabel.setUnderline(0.1f, -2f);
            manageUnitPhrase.add(manageUnitLabel);
            manageUnitPhrase.add(new Chunk(" : ", DEFAULT_FONT));
            PdfUtilities.addCell(sub, manageUnitPhrase);


            PdfUtilities.addCell(sub, new Phrase(bill.order.establishment.get().establishment.corporateName, DEFAULT_FONT));

            PdfPCell manageUnit = new PdfPCell(sub);
            manageUnit.setBorder(0);
            manageUnit.setColspan(2);
            t.addCell(manageUnit);
        }

        if (bill.order.market.isPresent()) {
            totalSizeUse += 2;
            Phrase refMarketPhrase = new Phrase();
            Chunk refMarketLabel = new Chunk("Référence marché", DEFAULT_FONT);
            refMarketLabel.setUnderline(0.1f, -2f);
            refMarketPhrase.add(refMarketLabel);
            refMarketPhrase.add(new Chunk(" : ", DEFAULT_FONT));
            refMarketPhrase.add(new Chunk(bill.order.market.get().marketNumber, DEFAULT_FONT));
            PdfPCell cRefMarket = new PdfPCell(refMarketPhrase);
            cRefMarket.setBorder(0);
            cRefMarket.setColspan(2);
            t.addCell(cRefMarket);
        }

        PdfPCell principal;
        //TODO implement algo when principal was implented
        if (false) {
            Phrase principalPhrase = new Phrase();
            Chunk principalLabel = new Chunk("Donneur d'ordre", DEFAULT_FONT);
            principalLabel.setUnderline(0.1f, -2f);
            principalPhrase.add(principalLabel);
            principalPhrase.add(new Chunk(" : ", DEFAULT_FONT));
            principalPhrase.add(new Chunk("NULL", DEFAULT_FONT));
            principal = new PdfPCell(principalPhrase);
        } else {
            principal = new PdfPCell(new Phrase(
                    "A la demande",
                    DEFAULT_FONT
            ));
        }

        principal.setBorder(0);
        principal.setColspan(2);
        t.addCell(principal);
        totalSizeUse += 2;

        if (false) {
            Phrase commentPhrase = new Phrase();
            Chunk commentLabel = new Chunk("Champ commentaire", DEFAULT_FONT);
            commentLabel.setUnderline(0.1f, -2f);
            commentPhrase.add(commentLabel);
            commentPhrase.add(new Chunk(" : ", DEFAULT_FONT));
            commentPhrase.add(new Chunk("NULL", DEFAULT_FONT));
            PdfPCell comment = new PdfPCell(commentPhrase);
            comment.setBorder(0);
            comment.setColspan(2);
            t.addCell(comment);
            totalSizeUse += 2;
        }

        if (totalSizeUse % 4 != 0) {
            PdfPCell cell = new PdfPCell();
            cell.setBorder(0);
            cell.setColspan(4 - (4 - (totalSizeUse / 4)));
            t.addCell(cell);
        }

        return t;
    }
}
