package pdf.pdfdocument;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BasicBillEvent extends PdfPageEventHelper {

    private final Logger logger = LoggerFactory.getLogger(BasicBillEvent.class);

    private PdfTemplate nbTotalPageTemplate;

    private Image nbTotalPageImg;

    public BasicBillEvent() {}

    @Override
    public void onOpenDocument(PdfWriter writer, Document document) {
        // On initialise un canvas pour écrire le numero de page nbTotalPageImg
        nbTotalPageTemplate = writer.getDirectContent().createTemplate(30, 16);
        try {
            nbTotalPageImg = Image.getInstance(nbTotalPageTemplate);
            nbTotalPageImg.setRole(PdfName.ARTIFACT);
        } catch (DocumentException de) {
            throw new ExceptionConverter(de);
        }
    }

    @Override
    public void onStartPage(PdfWriter writer, Document document) {
        super.onStartPage(writer, document);
    }

    @Override
    public void onEndPage(PdfWriter writer, Document document) {
        addPageNumber(writer, document);
    }

    @Override
    public void onCloseDocument(PdfWriter writer, Document document) {
        //On calcul la taille de la chaine de caractère qui correspond à la taille du nb de page
        int totalLength = String.valueOf(writer.getPageNumber()).length();
        int totalWidth = totalLength * 5;
        //On écrit le nb de page total
        ColumnText.showTextAligned(nbTotalPageTemplate, Element.ALIGN_RIGHT,
                new Phrase(String.valueOf(writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)),
                totalWidth, 6, 0);
    }

    /**
     * Methode pour ajouter le nombre de page dans le document courant
     *
     * @param writer
     */
    private void addPageNumber(PdfWriter writer, Document document) {
        PdfPTable tablePageNumber = new PdfPTable(2);
        try {
            tablePageNumber.setWidths(new int[]{26, 1});
        } catch (DocumentException de) {
            logger.warn("Impossible to initialise width of page number table", de);
        }

        Rectangle rectangle = document.getPageSize();
        int largeur = (int) rectangle.getWidth();

        tablePageNumber.setTotalWidth((float)largeur - ((int) (largeur * 0.12)));
        tablePageNumber.setLockedWidth(true);
        tablePageNumber.getDefaultCell().setBorder(0);

        // ajout du numero de page actuelle
        tablePageNumber.getDefaultCell().setHorizontalAlignment(Element.ALIGN_RIGHT);
        tablePageNumber.addCell(new Phrase(String.format("Page %d /", writer.getPageNumber()), new Font(Font.FontFamily.HELVETICA, 8)));

        // ajout de la zone pour écrire le numero de page nbTotalPageImg
        PdfPCell totalPageCount = new PdfPCell(nbTotalPageImg);
        totalPageCount.setBorder(0);
        tablePageNumber.addCell(totalPageCount);

        // on ecrit le canvas das la page
        PdfContentByte canvas = writer.getDirectContent();
        canvas.beginMarkedContentSequence(PdfName.ARTIFACT);
        tablePageNumber.writeSelectedRows(0, -1, 35, 35, canvas);
        canvas.endMarkedContentSequence();

    }
}
