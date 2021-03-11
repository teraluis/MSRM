package pdf.pdfbody;

import addresses.Address;
import addresses.AddressesService;
import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import estateclient.Annex;
import estateclient.Estate;
import estateclient.Locality;
import estateclient.Premises;
import missionclient.interventions.DoneIntervention;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pdf.pdfbody.bean.SubFooterLine;
import pdf.pdfbody.bean.TableData;
import pdf.pdfbody.bean.TableHeader;
import pdf.pdfbody.generic.BodyPdfHeaderInfo;
import pdf.pdfbody.generic.BodyPdfTableBody;
import pdf.pdfbody.generic.BodyPdfTableHeader;
import pdf.pdfdocument.PreviousSubTotalEvent;
import pdf.pdfdocument.Totals;
import pdf.pdfelement.AbstractPdfElement;
import pdf.utils.PdfUtilities;
import pdf.utils.SearchProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static pdf.pdfdocument.BasicBillDocument.DEFAULT_FONT;
import static pdf.utils.PdfUtilities.invisibleCell;

public class BasicBillBody extends AbstractPdfElement {
    private final Logger log = LoggerFactory.getLogger(BasicBillBody.class);

    private static final List<TableHeader> HEADER_BEANS = Arrays.asList(
            new TableHeader("N°", 3),
            new TableHeader("Code article ADX", 15),
            new TableHeader("Code article client", 25),
            new TableHeader("Désignation client", 25),
            new TableHeader("P.U HT", 9),
            new TableHeader("Qte", 7),
            new TableHeader("Remise (%)", 10),
            new TableHeader("Montant HT", 11, true),
            new TableHeader("Taux TVA", 11),
            new TableHeader("Total TTC", 11)
    );

    private static final List<TableData> DATA_BEANS = Arrays.asList(
            new TableData("number", Element.ALIGN_CENTER),
            new TableData("reference"),
            new TableData("customerReference"),
            new TableData("customerLabel"),
            new TableData("unitPrice", Element.ALIGN_RIGHT),
            new TableData("quantity", Element.ALIGN_CENTER),
            new TableData("discount", Element.ALIGN_RIGHT),
            new TableData("totalPrice", Element.ALIGN_RIGHT),
            new TableData("tax", Element.ALIGN_RIGHT),
            new TableData("totalPriceTaxInclude", Element.ALIGN_RIGHT)
    );

    protected List<Map<String, Object>> billDatas;
    private final FactureWithDetails bill;
    private final DoneIntervention intervention;
    private final SearchProperty search;
    private final Set<Estate> estates;
    private final AddressesService addressService;
    private final String orga;

    public BasicBillBody(
            Document document,
            List<Map<String, Object>> billDatas,
            FactureWithDetails bill,
            DoneIntervention intervention,
            SearchProperty search,
            Set<Estate> estates,
            AddressesService addressService,
            String orga
    ) {
        super(document);
        this.billDatas = billDatas;
        this.bill = bill;
        this.intervention = intervention;
        this.search = search;
        this.estates = estates;
        this.addressService = addressService;
        this.orga = orga;
    }

    @Override
    public List<Element> generateContent() {

        Totals totals = new Totals();

        List<Element> elements = new ArrayList<>();

        PdfPTable table = new PdfPTable(1);
        table.setHeaderRows(1);

        PdfUtilities.addCell(table, BodyPdfHeaderInfo.arrayHeaderInfo(bill, intervention, search));

        PdfPTable tableDescription = new PdfPTable(3);

        // TODO Gérer le multi biens
        Object entity = search.findEntityByBillLineId(bill.bill.lignes.get(0).uuid);
        String estateRef = buildReferencePatrimony(entity);

        Phrase addressPhrase = new Phrase();
        Chunk addressLabel = new Chunk("Adresse", DEFAULT_FONT);
        addressLabel.setUnderline(0.1f, -2f);
        addressPhrase.add(addressLabel);
        addressPhrase.add(new Chunk(" : ", DEFAULT_FONT));
        addressPhrase.add(new Chunk(intervention.estateAddress.get(), DEFAULT_FONT));
        if (estateRef != null && !estateRef.isEmpty()) {
            addressPhrase.add(new Chunk(" (" + estateRef + ")", DEFAULT_FONT));
        }
        PdfPCell addressCell = new PdfPCell(addressPhrase);
        addressCell.setBorder(0);
        if (estates.isEmpty() || true) {
            addressCell.setColspan(3);
        }

        if (!estates.isEmpty() || true) {
            addressCell.setColspan(2);
        }
        PdfUtilities.addCell(tableDescription, addressCell);

        if (!estates.isEmpty()) {
            Estate estate = estates.stream().findFirst().get();
            if(estate.estateReference.isPresent()) {
                Phrase referencePhrase = new Phrase();
                Chunk referenceLabel = new Chunk("Référence patrimoine", DEFAULT_FONT);
                referenceLabel.setUnderline(0.1f, -2f);
                referencePhrase.add(referenceLabel);
                referencePhrase.add(new Chunk(" : ", DEFAULT_FONT));
                referencePhrase.add(new Chunk(estate.estateReference.get(), DEFAULT_FONT));
                PdfUtilities.addCell(tableDescription, referencePhrase);
            }
        }

        if (false) {
            Phrase commentPhrase = new Phrase();
            Chunk commentLabel = new Chunk("Champ commentaire libre", DEFAULT_FONT);
            commentLabel.setUnderline(0.1f, -2f);
            commentPhrase.add(commentLabel);
            commentPhrase.add(new Chunk(" : ", DEFAULT_FONT));
            commentPhrase.add(new Chunk(estates.stream().findFirst().get().estateReference.get(), DEFAULT_FONT));
            PdfPCell commentCell = new PdfPCell(commentPhrase);
            commentCell.setBorder(0);
            if (estates.isEmpty()) {
                commentCell.setColspan(2);
            }
            PdfUtilities.addCell(tableDescription, commentCell);
        } else {
            PdfUtilities.addCell(tableDescription, invisibleCell());
        }

        tableDescription.setSpacingAfter(2);
        PdfUtilities.addCell(table, tableDescription);

        PdfPTable tableArray = new PdfPTable(HEADER_BEANS.size());
        setTableRelativeWidths(tableArray);
        tableArray.setHeaderRows(3);
        tableArray.setFooterRows(1);
        tableArray.setSkipLastFooter(true);
        tableArray.setSkipFirstHeader(true);

        BodyPdfTableHeader.arrayHeader(tableArray, HEADER_BEANS);

        PdfPCell subHeader = new PdfPCell(new Phrase("Total intermédiaire HT précédant", new Font(Font.FontFamily.UNDEFINED, 6)));
        subHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        subHeader.setBorder(0);
        subHeader.setBorderWidthBottom(0.5f);
        subHeader.setBorderWidthLeft(0.5f);
        subHeader.setColspan(HEADER_BEANS.size() - 1);
        tableArray.addCell(subHeader);

        PdfPCell subHeaderValue = new PdfPCell();
        subHeaderValue.setBorder(0);
        subHeaderValue.setBorderWidthBottom(0.5f);
        subHeaderValue.setBorderWidthLeft(0.5f);
        subHeaderValue.setBorderWidthRight(0.5f);
        subHeaderValue.setCellEvent(new PreviousSubTotalEvent(totals));
        tableArray.addCell(subHeaderValue);

        new SubFooterLine(tableArray, 5, "Total intermédiaire HT", totals);

        BodyPdfTableHeader.arrayHeader(tableArray, HEADER_BEANS);

        BodyPdfTableBody.arrayBody(tableArray, DATA_BEANS, billDatas, totals);

        PdfUtilities.addCell(table, tableArray);

        elements.add(table);

        return elements;
    }

    private void setTableRelativeWidths(PdfPTable table) {
        List<Integer> relativeWidthsList = new ArrayList<>(HEADER_BEANS.size());
        for (TableHeader header : HEADER_BEANS) {
            relativeWidthsList.add(header.getPercentWith());
        }
        int[] relativeWidths = relativeWidthsList.stream().mapToInt(i -> i).toArray();
        try {
            table.setWidths(relativeWidths);
        } catch (DocumentException e) {
            log.error("Whoops failed while setting width of table", e);
        }
    }

    private String buildReferencePatrimony(Object entity) {
        if (entity instanceof Locality) {
            return buildLocalityInfo((Locality) entity);
        } else if (entity instanceof Premises) {
            return buildPremiseInfo((Premises) entity);
        } else {
            return buildAnnexInfo((Annex) entity);
        }
    }

    private String buildLocalityInfo(Locality locality) {

        return locality.name +
                " " +
                String.join(" ,", locality.addresses.stream().map(this::buildAddress).collect(Collectors.toSet()));
    }

    private String buildAddress(String addressId) {
        Optional<Address> address;
        try {
            address = addressService.get(orga, addressId).toCompletableFuture().get();
        } catch (InterruptedException | ExecutionException e) {
            log.error("Error while retrieving address", e);
            address = Optional.empty();
        }

        if (!address.isPresent()) {
            return null;
        }

        Address addr = address.get();

        StringBuilder sb = new StringBuilder();

        addr.address1.ifPresent(a -> sb.append(a).append(" "));
        addr.address2.ifPresent(a -> sb.append(a).append(" "));
        addr.postCode.ifPresent(a -> sb.append(a).append(" "));
        addr.city.ifPresent(a -> sb.append(a).append(" "));

        return sb.toString();
    }

    private String buildPremiseInfo(Premises premises) {

        List<String> sb = new ArrayList<>();

        premises.premisesReference.ifPresent(s -> sb.add("Ref: " + s));

        if (premises.number != null && !premises.number.isEmpty()) {
            sb.add("Appt : " + premises.number);
        }

        premises.floor.ifPresent(s -> sb.add("Etage : " + s));

        if (premises.premisesType.type != null && !premises.premisesType.type.isEmpty()) {
            sb.add("Type : " + premises.premisesType.type);
        }

        if (sb.isEmpty()) {
            return "";
        }

        return String.join(", ", sb);
    }

    private String buildAnnexInfo(Annex annex) {
        StringBuilder sb = new StringBuilder();

//        sb.append("Ref : ");
//        sb.append(annex.annexReference.orElse(""));
        sb.append("Etage : ");
        sb.append(annex.floor);
        if (annex.annexType.isPresent() || annex.customAnnexType.isPresent()) {
            sb.append(" ,Type : ");
            if (annex.annexType.isPresent() && annex.customAnnexType.isPresent()) {
                sb.append(annex.annexType.get());
                sb.append(", ");
                sb.append(annex.customAnnexType.get());
            } else {
                annex.annexType.ifPresent(t -> sb.append(t.type));
                annex.customAnnexType.ifPresent(sb::append);
            }

        }

        return sb.toString();
    }
}
