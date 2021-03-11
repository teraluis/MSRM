package utils.VariablesExport;

import api.v1.models.Avoir;
import api.v1.models.Facture;
import api.v1.models.FactureLigne;
import api.v1.models.User;
import core.CsvLine;
import core.models.AddressWithRole;
import establishments.EstablishmentAddressRole;
import missionclient.Asbestos;
import missionclient.Expert;
import missionclient.interventions.DoneIntervention;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class BillCsvLine extends CsvLine {

    public static HashMap<String, String> generateValuesFromBill(DoneIntervention mission, Facture bill, User commercial, List<Asbestos> asbestosResults, api.v1.models.Order order) {
        HashMap<String, String> values = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        Integer nbPrelev = 0;
        for (Asbestos asbestos : asbestosResults) {
            nbPrelev = nbPrelev + asbestos.analyseCount.get();
        }
        BigDecimal analysisUnitPrice = BigDecimal.ZERO;
        BigDecimal analysisPrice = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        Integer nbAnalyses = 0;
        List<String> offres = new ArrayList<>();
        for (FactureLigne line : bill.lignes) {
            total = total.add(line.total);
            offres.add(line.refadx);
            if (line.refadx.equalsIgnoreCase("ANALYSE")) {
                analysisUnitPrice = line.total.divide(new BigDecimal(line.quantity), 4, RoundingMode.HALF_UP);
                analysisPrice = line.total;
                nbAnalyses = line.quantity;
            }
        }
        String codeOffre = offres.stream().collect(Collectors.joining(","));
        String prescriberLabel;
        String prescriberName;
        String prescriberAddress1;
        String prescriberAddress2;
        String prescriberPostCode;
        String prescriberCity;
        String clientCode = order.establishment.map(e -> e.establishment.sageCode).orElse(""); // TODO sageCode or legacyCode ?
        Optional<AddressWithRole> firstAddress;
        if (order.establishment.isPresent()) {
            prescriberName = order.establishment.get().establishment.name;
            firstAddress = order.establishment.get().addresses.stream().anyMatch(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString()))
                    ? order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString())).findAny()
                    : order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.MAIN.toString())).findAny();
        } else {
            // TODO manage individuals
            prescriberName = "";
            firstAddress = Optional.of(new AddressWithRole(null, null));
        }
        // Must have an address
        prescriberAddress1 = firstAddress.get().address.address1.orElse("");
        prescriberAddress2 = firstAddress.get().address.address2.orElse("");
        prescriberPostCode = firstAddress.get().address.postCode.orElse("");
        prescriberCity = firstAddress.get().address.city.orElse("");
        prescriberLabel = prescriberName + " " + prescriberPostCode + " " + prescriberCity;

        for (String column : BillCsvFileUtils.getColumns()) {
            if (column.equals("id entite")) {
                values.put(column, "15");
            } else if (column.equals("entite")) {
                values.put(column, "AGENCE NORMANDIE");
            } else if (column.equals("date facture")) {
                values.put(column, dateFormat.format(bill.exportDate.get()));
            } else if (column.equals("numero mission")) {
                values.put(column, order.name);
            } else if (column.equals("Expert Facture")) {
                Expert expert = mission.getPlanning().getExpert();
                values.put(column, expert.firstname + " " + expert.lastname);
            } else if (column.equals("Total TTC")) {
                values.put(column, df.format(total));
            } else if (column.equals("nb prlv DI")) {
                values.put(column, nbPrelev.toString());
            } else if (column.equals("nb prlv AA")) {
                values.put(column, nbAnalyses.toString());
            } else if (column.equals("pu TTC prlv")) {
                values.put(column, df.format(analysisUnitPrice));
            } else if (column.equals("total TTC prlv")) {
                values.put(column, df.format(analysisPrice));
            } else if (column.equals("numero Facture")) {
                values.put(column, bill.name);
            } else if (column.equals("commercial")) {
                values.put(column, commercial.first_name + " " + commercial.last_name);
            } else if (column.equals("code offre")) {
                values.put(column, codeOffre);
            } else if (column.equals("date mission")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("CC GC")) {
                values.put(column, "GC");
            } else if (column.equals("taux TVA")) {
                values.put(column, "20");
            } else if (column.equals("Origine Business")) {
                values.put(column, "GCL"); // TODO : change to Calypso when clients are in Calypso
            } else if (column.equals("Prescripteur")) {
                values.put(column, prescriberLabel);
            } else if (column.equals("prsc nom")) {
                values.put(column, prescriberName);
            } else if (column.equals("prsc adresse 1")) {
                values.put(column, prescriberAddress1);
            } else if (column.equals("prsc adresse 2")) {
                values.put(column, prescriberAddress2);
            } else if (column.equals("prsc CP")) {
                values.put(column, prescriberPostCode);
            } else if (column.equals("prsc Ville")) {
                values.put(column, prescriberCity);
            } else if (column.equals("prsc id")) {
                values.put(column, clientCode);
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public static HashMap<String, String> generateValuesFromCreditNote(DoneIntervention mission, Avoir creditnote, Facture billAssociated, User commercial, List<Asbestos> asbestosResults, api.v1.models.Order order) {
        HashMap<String, String> values = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        Integer nbPrelev = 0;
        for (Asbestos asbestos : asbestosResults) {
            nbPrelev = nbPrelev + asbestos.analyseCount.get();
        }
        BigDecimal analysisUnitPrice = BigDecimal.ZERO;
        BigDecimal analysisPrice = BigDecimal.ZERO;
        BigDecimal total = BigDecimal.ZERO;
        Integer nbAnalyses = 0;
        List<String> offres = new ArrayList<>();
        for (FactureLigne line : billAssociated.lignes) {
            total = total.add(line.total);
            offres.add(line.refadx);
            if (line.refadx.equalsIgnoreCase("ANALYSE")) {
                analysisUnitPrice = line.total.divide(new BigDecimal(line.quantity), 4, RoundingMode.HALF_UP);
                analysisPrice = line.total;
                nbAnalyses = line.quantity;
            }
        }
        String codeOffre = offres.stream().collect(Collectors.joining(","));
        String prescriberLabel;
        String prescriberName;
        String prescriberAddress1;
        String prescriberAddress2;
        String prescriberPostCode;
        String prescriberCity;
        String clientCode = order.establishment.map(e -> e.establishment.sageCode).orElse(""); // TODO sageCode or legacyCode ?
        Optional<AddressWithRole> firstAddress;
        if (order.establishment.isPresent()) {
            prescriberName = order.establishment.get().establishment.name;
            firstAddress = order.establishment.get().addresses.stream().anyMatch(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString()))
                    ? order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString())).findAny()
                    : order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.MAIN.toString())).findAny();
        } else {
            // TODO manage individuals
            prescriberName = "";
            firstAddress = Optional.of(new AddressWithRole(null, null));
        }
        // Must have an address
        prescriberAddress1 = firstAddress.get().address.address1.orElse("");
        prescriberAddress2 = firstAddress.get().address.address2.orElse("");
        prescriberPostCode = firstAddress.get().address.postCode.orElse("");
        prescriberCity = firstAddress.get().address.city.orElse("");
        prescriberLabel = prescriberName + " " + prescriberPostCode + " " + prescriberCity;
        nbAnalyses = nbAnalyses * -1;
        analysisPrice = analysisPrice.multiply(new BigDecimal(-1));
        total = total.multiply(new BigDecimal(-1));
        for (String column : BillCsvFileUtils.getColumns()) {
            if (column.equals("id entite")) {
                values.put(column, "15");
            } else if (column.equals("entite")) {
                values.put(column, "AGENCE NORMANDIE");
            } else if (column.equals("date facture")) {
                values.put(column, dateFormat.format(creditnote.exportdate.get()));
            } else if (column.equals("refacturation")) {
                values.put(column, "oui");
            } else if (column.equals("numero mission")) {
                values.put(column, order.name);
            } else if (column.equals("Expert Facture")) {
                Expert expert = mission.getPlanning().getExpert();
                values.put(column, expert.firstname + " " + expert.lastname);
            } else if (column.equals("Total TTC")) {
                values.put(column, df.format(total));
            } else if (column.equals("nb prlv DI")) {
                values.put(column, nbPrelev.toString());
            } else if (column.equals("nb prlv AA")) {
                values.put(column, nbAnalyses.toString());
            } else if (column.equals("pu TTC prlv")) {
                values.put(column, df.format(analysisUnitPrice));
            } else if (column.equals("total TTC prlv")) {
                values.put(column, df.format(analysisPrice));
            } else if (column.equals("numero Facture")) {
                values.put(column, creditnote.name);
            } else if (column.equals("commercial")) {
                values.put(column, commercial.first_name + " " + commercial.last_name);
            } else if (column.equals("code offre")) {
                values.put(column, codeOffre);
            } else if (column.equals("date mission")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("CC GC")) {
                values.put(column, "GC");
            } else if (column.equals("taux TVA")) {
                values.put(column, "20");
            } else if (column.equals("Origine Business")) {
                values.put(column, "GCL"); // TODO : change to Calypso when clients are in Calypso
            } else if (column.equals("Prescripteur")) {
                values.put(column, prescriberLabel);
            } else if (column.equals("prsc nom")) {
                values.put(column, prescriberName);
            } else if (column.equals("prsc adresse 1")) {
                values.put(column, prescriberAddress1);
            } else if (column.equals("prsc adresse 2")) {
                values.put(column, prescriberAddress2);
            } else if (column.equals("prsc CP")) {
                values.put(column, prescriberPostCode);
            } else if (column.equals("prsc Ville")) {
                values.put(column, prescriberCity);
            } else if (column.equals("prsc id")) {
                values.put(column, clientCode);
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public BillCsvLine(HashMap<String, String> values) {
        super(values);
    }

}
