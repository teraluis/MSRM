package pdf.pdffooter.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import missionclient.interventions.DoneIntervention;
import pdf.utils.BillUtilities;
import pdf.utils.DashedBorder;
import pdf.utils.PdfUtilities;

import java.util.Date;

public class FooterPdfDetachablePart {

    public static PdfPTable detachablePart(FactureWithDetails bill, DoneIntervention intervention) {
        PdfPTable t = new PdfPTable(1);

        Phrase pDetachablePart = new Phrase();
        pDetachablePart.setFont(new Font(Font.FontFamily.HELVETICA, 6, Font.BOLD | Font.ITALIC, BaseColor.BLACK));
        pDetachablePart.add("Papillon à joindre au réglement");

        PdfPCell cDetachablePart = new PdfPCell(pDetachablePart);
        cDetachablePart.setBorder(0);
        cDetachablePart.setHorizontalAlignment(Element.ALIGN_CENTER);

        t.addCell(cDetachablePart);

        float[] colsWidth = {2, 1};
        PdfPTable infos = new PdfPTable(colsWidth);

        Phrase pBillNumber = new Phrase();
        pBillNumber.setFont(new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK));
        pBillNumber.add(bill.bill.name);

        PdfPCell cBillNumber = new PdfPCell(pBillNumber);
        cBillNumber.setBorder(0);
        cBillNumber.setColspan(2);
        cBillNumber.setHorizontalAlignment(Element.ALIGN_CENTER);
        cBillNumber.setBackgroundColor(BaseColor.LIGHT_GRAY);

        infos.addCell(cBillNumber);

        Phrase pDetachableInfo = new Phrase();
        pDetachableInfo.add(new Chunk("Date de la facture:\n\n", new Font(Font.FontFamily.HELVETICA, 8, Font.UNDERLINE | Font.BOLD, BaseColor.BLACK)));
        pDetachableInfo.add(new Chunk("N° de client:\n\n", new Font(Font.FontFamily.HELVETICA, 8, Font.UNDERLINE | Font.BOLD, BaseColor.BLACK)));
        pDetachableInfo.add(new Chunk("Restant dù:\n", new Font(Font.FontFamily.HELVETICA, 8, Font.UNDERLINE | Font.BOLD, BaseColor.BLACK)));
        PdfPCell cDetachableInfo = new PdfPCell(pDetachableInfo);
        cDetachableInfo.setBorder(0);
        cDetachableInfo.setHorizontalAlignment(Element.ALIGN_LEFT);

        Phrase pDetachableInfoData = new Phrase();
        pDetachableInfoData.add(
                new Chunk(
                        String.format("%s%n%n", PdfUtilities.formatDate(new Date())),
                        new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK)
                )
        );
        pDetachableInfoData.add(
                new Chunk(
                        String.format("%s%n%n", bill.order.establishment.get().account.reference),
                        new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK)
                )
        );
        pDetachableInfoData.add(
                new Chunk(
                        String.format("%s%n", PdfUtilities.df.format(BillUtilities.remainingBalance(bill))),
                        new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK)
                )
        );
        PdfPCell cDetachableInfoData = new PdfPCell(pDetachableInfoData);
        cDetachableInfoData.setBorder(0);
        cDetachableInfoData.setHorizontalAlignment(Element.ALIGN_LEFT);

        infos.addCell(cDetachableInfo);
        infos.addCell(cDetachableInfoData);

        PdfPCell detachableCell = new PdfPCell(infos);
        detachableCell.setBorder(0);
        detachableCell.setCellEvent(new DashedBorder(PdfPCell.BOX));
        detachableCell.setPadding(4);

        t.addCell(detachableCell);

        PdfUtilities.addCell(t, PdfUtilities.invisibleCell());

        return t;
    }
}
