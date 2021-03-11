package pdf.pdfdocument;

import addresses.AddressesService;
import api.v1.models.FactureLigne;
import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Font;
import estateclient.Estate;
import core.models.Prestation;
import missionclient.interventions.DoneIntervention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdf.pdfbody.BasicBillBody;
import pdf.pdfelement.AbstractPdfElement;
import pdf.pdffooter.BasicBillFooter;
import pdf.pdfheader.BasicBillHeader;
import pdf.utils.CurrencyCalculation;
import pdf.utils.SearchProperty;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static pdf.utils.CurrencyCalculation.divide;
import static pdf.utils.CurrencyCalculation.multiply;
import static pdf.utils.CurrencyCalculation.subtract;

public class BasicBillDocument extends AbstractPdfDocument {
    private final Logger log = LoggerFactory.getLogger(BasicBillDocument.class);
    public static final Font DEFAULT_FONT = new Font(Font.FontFamily.HELVETICA, 8);

    private final String orga;
    private final FactureWithDetails bill;
    private final Set<Prestation> prestations;
    private final Set<Estate> estates;

    private final AddressesService addressService;

    public BasicBillDocument(
            AddressesService addressService,
            String orga,
            FactureWithDetails bill,
            Set<Prestation> prestations,
            Set<Estate> estates
    ) {
        super();
        if (prestations.isEmpty()) {
            throw new NullPointerException("No prestation associated with this bill");
        }

        if (estates.isEmpty()) {
            throw new NullPointerException("No estate associated with this bill");
        }
        this.orga = orga;
        this.bill = bill;
        this.prestations = prestations;
        this.estates = estates;
        this.addressService = addressService;
    }

    @Override
    protected AbstractPdfElement getHeader() {
        return new BasicBillHeader(this, bill);
    }

    @Override
    protected AbstractPdfElement getBody() {
        List<Map<String, Object>> billDatas = new ArrayList<>();
        SearchProperty search = new SearchProperty(estates, prestations);
        int index = 1;
        for (FactureLigne fl : bill.bill.lignes) {
            Map<String, Object> billData1 = new HashMap<>();
            BigDecimal total = multiply(
                    subtract(
                            fl.price,
                            multiply(
                                    fl.price,
                                    divide(
                                            fl.discount,
                                            new BigDecimal(100)
                                    )
                            )
                    ),
                    new BigDecimal(fl.quantity)
            );

            BigDecimal totalWithTax =
                    multiply(
                            multiply(
                                    subtract(
                                            fl.price,
                                            multiply(
                                                    fl.price,
                                                    divide(
                                                            fl.discount,
                                                            new BigDecimal(100)
                                                    )
                                            )
                                    ),
                                    CurrencyCalculation.add(
                                            divide(
                                                    getTVA(fl.tvacode),
                                                    new BigDecimal(100)
                                            ),
                                            new BigDecimal(1)
                                    )
                            ),
                            new BigDecimal(fl.quantity)
                    );
            billData1.put("number", index);
            billData1.put("reference", fl.refadx);
            billData1.put("customerReference", fl.refbpu.orElse(""));
            billData1.put("customerLabel", fl.designation.orElse("Pas de désignation"));
            billData1.put("unitPrice", fl.price);
            billData1.put("quantity", fl.quantity);
            billData1.put("discount", fl.discount);
            billData1.put("totalPrice", total);
            billData1.put("tax", fl.tvacode);
            billData1.put("totalPriceTaxInclude", totalWithTax);
            billDatas.add(billData1);
            index++;
        }

        // TODO gérer le multi inter et multi estate
        return new BasicBillBody(this, billDatas, bill, (DoneIntervention) bill.interventions.get(0), search, estates, addressService, orga);
    }

    @Override
    protected AbstractPdfElement getFooter() {
        // TODO gérer le multi inter
        return new BasicBillFooter(this, bill, (DoneIntervention) bill.interventions.get(0));
    }

    // FIXME supprimer cette aberration
    private BigDecimal getTVA(String tva) {
        switch (tva) {
            case "20%":
                return new BigDecimal(20).setScale(4, RoundingMode.HALF_UP);
            case "10%":
                return BigDecimal.TEN.setScale(4, RoundingMode.HALF_UP);
            case "0%":
                return BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP);
            default:
                throw new IllegalArgumentException(String.format("No case for %s", tva));
        }
    }
}
