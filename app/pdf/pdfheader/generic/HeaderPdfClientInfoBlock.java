package pdf.pdfheader.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import pdf.utils.PdfUtilities;

import java.math.BigDecimal;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;

public class HeaderPdfClientInfoBlock {

    private static final Font HEADER_CLIENT_FONT = new Font(DEFAULT_FONT);

    private HeaderPdfClientInfoBlock() {
    }

    public static PdfPTable basicClientBlock(FactureWithDetails bill) {
        HEADER_CLIENT_FONT.setSize(10);
        PdfPTable wrapper = new PdfPTable(1);

        PdfPCell nameBilledCustomer = new PdfPCell(new Phrase(bill.order.establishment.get().establishment.name.toUpperCase(), HEADER_CLIENT_FONT));
        nameBilledCustomer.setBorder(0);
        wrapper.addCell(nameBilledCustomer);

        String sb = String.format("%s%n", PdfUtilities.getBillStreetAddress(bill.order.establishment.get().addresses)) +
                String.format("%s%n", PdfUtilities.getBillCity(bill.order.establishment.get().addresses)) +
                String.format("%s%n", PdfUtilities.getBillCountry(bill.order.establishment.get().addresses));
        PdfPCell addressBilledCustomer = new PdfPCell(new Phrase(sb, HEADER_CLIENT_FONT));
        addressBilledCustomer.setBorder(0);
        wrapper.addCell(addressBilledCustomer);

        if (bill.order.establishment.get().account.entity.isPresent()) {
            BigDecimal siren = new BigDecimal(bill.order.establishment.get().account.entity.get().siren.replaceAll("\\s+", ""));
            BigDecimal key = siren.remainder(new BigDecimal(97)).multiply(new BigDecimal(3)).add(new BigDecimal(12)).remainder(new BigDecimal(97));

            Phrase customerTVACodePhrase = new Phrase();
            Chunk customerTVACodeLabel = new Chunk("N° TVA IC", DEFAULT_FONT);
            customerTVACodeLabel.setUnderline(0.1f, -2f);
            customerTVACodePhrase.add(customerTVACodeLabel);
            customerTVACodePhrase.add(new Chunk(" : ", DEFAULT_FONT));
            customerTVACodePhrase.add(new Chunk(String.format("FR %s %s", key.toString(), bill.order.establishment.get().account.entity.get().siren), DEFAULT_FONT));
            PdfPCell spacing = new PdfPCell(customerTVACodePhrase);
            spacing.setPaddingTop(10);
            spacing.setBorder(0);
            PdfUtilities.addCell(wrapper, spacing);
        }

        Phrase clientIdPhrase = new Phrase();
        Chunk clientIdLabel = new Chunk("N° Client ADX", DEFAULT_FONT);
        clientIdLabel.setUnderline(0.1f, -2f);
        clientIdPhrase.add(clientIdLabel);
        clientIdPhrase.add(new Chunk(" : ", DEFAULT_FONT));
        clientIdPhrase.add(new Chunk(bill.order.establishment.get().account.reference, DEFAULT_FONT));
        PdfUtilities.addCell(wrapper, clientIdPhrase);

        return wrapper;
    }
}
