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
import pdf.utils.PdfUtilities;

import java.util.List;

public class FooterPdfInformationsPart {

    public static PdfPTable informationsPart(FactureWithDetails bill, DoneIntervention intervention) {
        PdfPTable t = new PdfPTable(1);

        Phrase pFirstInformations = new Phrase();
        pFirstInformations.add(new Chunk("Modes de réglement acceptés :\n", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK)));
        pFirstInformations.add(new Chunk("-chèque à l'ordre d'ADX GROUPE\n", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK)));
        pFirstInformations.add(new Chunk("- notre IBAN :\n", new Font(Font.FontFamily.HELVETICA, 8, Font.ITALIC, BaseColor.BLACK)));
        PdfPCell cFirstInformations = new PdfPCell(pFirstInformations);
        cFirstInformations.setBorder(0);
        cFirstInformations.setHorizontalAlignment(Element.ALIGN_LEFT);

        t.addCell(cFirstInformations);

        PdfPCell cIban = new PdfPCell(new Phrase("CIC ANGERS ENTREPRISES FR76 3004 7142 9300 0803 1670 116 / CMCIFRPP", new Font(Font.FontFamily.HELVETICA, 8, Font.NORMAL, BaseColor.BLACK)));
        cIban.setBorder(0);
        cIban.setHorizontalAlignment(Element.ALIGN_CENTER);

        t.addCell(cIban);

        float[] colsWidth = {5, 2};
        PdfPTable labelVirement = new PdfPTable(colsWidth);

        PdfPCell cLabel = new PdfPCell(new Phrase("Libellé du virement à mentionner IMPERATIVEMENT :", new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK)));
        cLabel.setBorder(0);
        cLabel.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelVirement.addCell(cLabel);

        PdfPCell cNumber = new PdfPCell(new Phrase(bill.bill.name, new Font(Font.FontFamily.HELVETICA, 8, Font.BOLD, BaseColor.BLACK)));
        cNumber.setBorder(0);
        cNumber.setBackgroundColor(BaseColor.LIGHT_GRAY);
        cNumber.setHorizontalAlignment(Element.ALIGN_CENTER);
        labelVirement.addCell(cNumber);

        PdfUtilities.addCell(t, labelVirement);

        PdfPCell cLegal = new PdfPCell(new Phrase("Conformément aux conditions générales de ventes, à défaut de paiement intégral dans le délai prévu pour leur règlement, les sommes dues seront majorées de plein droit de pénalités de de retard calculées sur la base d'un taux annuel de 12% appliqué au montant de la créance TTC. Ces pénalités ne peuvent être inférieures à un minimum de perception de 30,00 euros. Aucun escompte en cas de paiement anticipé.", new Font(Font.FontFamily.HELVETICA, 7, Font.BOLD | Font.ITALIC, BaseColor.BLACK)));
        cLegal.setBorder(0);
        cLegal.setPaddingTop(10);
        cLegal.setHorizontalAlignment(Element.ALIGN_CENTER);

        t.addCell(cLegal);

        return t;
    }
}
