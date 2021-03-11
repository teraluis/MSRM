package pdf.pdfheader.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;

public class HeaderPdfAdxInfoBlock {

    private HeaderPdfAdxInfoBlock() {
    }

    public static PdfPTable basicAdxInfo(FactureWithDetails bill) {
        PdfPTable t = new PdfPTable(new float[]{1.2f, 0.8f});

        Phrase pSocieteInfo = new Phrase();
        pSocieteInfo.add(new Chunk("ADX GROUPE\n", DEFAULT_FONT));
        pSocieteInfo.add(new Chunk("SAS au capital de 6 990 495€\n", DEFAULT_FONT));
        pSocieteInfo.add(new Chunk("TVA IC : FR50505037044\n", DEFAULT_FONT));
        PdfPCell cSocieteInfo = new PdfPCell(pSocieteInfo);
        cSocieteInfo.setBorder(0);
        t.addCell(cSocieteInfo);

        Phrase pSocieteInfo2 = new Phrase();
        pSocieteInfo2.add(new Chunk("\n", DEFAULT_FONT));
        pSocieteInfo2.add(new Chunk("SIREN : 505 037 044\n", DEFAULT_FONT));
        pSocieteInfo2.add(new Chunk("Code APE : 7112 B\n", DEFAULT_FONT));
        PdfPCell cSocieteInfo2 = new PdfPCell(pSocieteInfo2);
        cSocieteInfo2.setBorder(0);
        t.addCell(cSocieteInfo2);

        String agency;

        if(bill.order.agency.isPresent()) {
            agency = bill.order.agency.get().name;
        } else {
            agency = bill.order.market.get().agency.name;
        }

        Phrase pEntite = new Phrase();
        pEntite.add(new Chunk(String.format("Agence de prodution %s%n", agency), DEFAULT_FONT));
        pEntite.add(new Chunk("Parc Saint Fiacre\n", DEFAULT_FONT));
        pEntite.add(new Chunk("53200 Château-Gontier\n", DEFAULT_FONT));
        pEntite.add(new Chunk("Tél agence : 09 70 69 07 49\n", DEFAULT_FONT));

        pEntite.add(new Chunk("\n", DEFAULT_FONT));
        PdfPCell cEntite = new PdfPCell(pEntite);
        cEntite.setColspan(2);
        cEntite.setBorder(0);
        t.addCell(cEntite);

        return t;
    }
}
