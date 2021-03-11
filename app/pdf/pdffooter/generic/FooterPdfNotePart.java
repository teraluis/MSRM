package pdf.pdffooter.generic;

import api.v1.models.FactureWithDetails;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import missionclient.interventions.DoneIntervention;
import pdf.utils.PdfUtilities;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjuster;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class FooterPdfNotePart {

    public static final Font MAIN_FONT = new Font(Font.FontFamily.HELVETICA, 8);

    public static PdfPTable notePart(FactureWithDetails bill) {
        PdfPTable t = new PdfPTable(1);

        PdfPTable subTable = new PdfPTable(2);

        PdfPCell deadlineString = new PdfPCell(new Phrase("Date d'échéance", MAIN_FONT));
        deadlineString.setBorder(0);
        subTable.addCell(deadlineString);

        Instant deadlineDate = bill.bill.exportDate.orElse(new Date()).toInstant();
        LocalDate lastDayOfMonth = deadlineDate.plus(30, ChronoUnit.DAYS).atZone(ZoneOffset.UTC).toLocalDate().with(TemporalAdjusters.lastDayOfMonth());

        PdfPCell deadline = new PdfPCell(
                new Phrase(PdfUtilities.formatDate(lastDayOfMonth), MAIN_FONT)
        );
        deadline.setBorder(0);
        deadline.setHorizontalAlignment(Element.ALIGN_LEFT);
        subTable.addCell(deadline);

        PdfPCell paymentModeString = new PdfPCell(new Phrase("Mode de règlement", MAIN_FONT));
        paymentModeString.setBorder(0);
        subTable.addCell(paymentModeString);

        PdfPCell paymentMode = new PdfPCell(new Phrase("Virement", MAIN_FONT));
        paymentMode.setBorder(0);
        paymentMode.setHorizontalAlignment(Element.ALIGN_LEFT);
        subTable.addCell(paymentMode);

        PdfPCell subTableCell = new PdfPCell(subTable);
        subTableCell.setBorder(0);
        t.addCell(subTableCell);

        // Service Period
        PdfPTable largeTable = new PdfPTable(new float[]{9f, 1f});
        PdfPTable servicePeriodTable = new PdfPTable(1);

        PdfPCell servicePeriodTitle = new PdfPCell(new Phrase("Période de l'intervention", MAIN_FONT));
        servicePeriodTitle.setHorizontalAlignment(Element.ALIGN_CENTER);
        servicePeriodTable.addCell(servicePeriodTitle);

        PdfPTable servicePeriodSubTable = new PdfPTable(2);

        List<DoneIntervention> interventions = bill.interventions.stream().map(i -> (DoneIntervention) i).collect(Collectors.toList());
        Optional<Date> startDate = interventions.stream().map(i -> i.getParameters().getInterventionDate()).min(Date::compareTo);
        Optional<Date> endDate = interventions.stream().map(i -> i.getParameters().getInterventionDate()).max(Date::compareTo);

        PdfPCell startDateCell = new PdfPCell(new Phrase(
                PdfUtilities.formatDate(startDate.get()),
                MAIN_FONT
        ));
        startDateCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        servicePeriodSubTable.addCell(startDateCell);
        PdfPCell stopDate = new PdfPCell(new Phrase(
                PdfUtilities.formatDate(endDate.get()),
                MAIN_FONT
        ));
        stopDate.setHorizontalAlignment(Element.ALIGN_CENTER);
        servicePeriodSubTable.addCell(stopDate);
        PdfPCell servicePeriodSubCell = new PdfPCell(servicePeriodSubTable);
        servicePeriodSubCell.setBorder(0);
        servicePeriodTable.addCell(servicePeriodSubCell);

        PdfPCell servicePeriodCell = new PdfPCell(servicePeriodTable);
        servicePeriodCell.setBorder(0);
        largeTable.addCell(servicePeriodCell);
        largeTable.addCell(PdfUtilities.invisibleCell());
        PdfPCell largeCell = new PdfPCell(largeTable);
        largeCell.setBorder(0);
        t.addCell(largeCell);

        if (bill.order.establishment.isPresent()) {
            PdfPCell cFirstInformations = new PdfPCell(new Phrase(
                    new Chunk(
                            String.format("SIRET client facturé : %s%n", bill.order.establishment.get().establishment.siret),
                            MAIN_FONT
                    )
            ));
            cFirstInformations.setBorder(0);
            cFirstInformations.setHorizontalAlignment(Element.ALIGN_LEFT);

            t.addCell(cFirstInformations);
        }

        return t;
    }
}
