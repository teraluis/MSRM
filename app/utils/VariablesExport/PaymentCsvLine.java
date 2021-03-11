package utils.VariablesExport;

import api.v1.models.Facture;
import api.v1.models.FactureLigne;
import api.v1.models.Paiement;
import api.v1.models.User;
import core.CsvLine;
import missionclient.Expert;
import missionclient.interventions.DoneIntervention;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PaymentCsvLine extends CsvLine {

    public static HashMap<String, String> generateValues(DoneIntervention mission, Facture bill, Paiement payment, api.v1.models.Order order, User commercial, Integer number) {
        HashMap<String, String> values = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        List<String> offres = new ArrayList<>();
        for (FactureLigne line : bill.lignes) {
            offres.add(line.refadx);
        }
        String codeOffre = offres.stream().collect(Collectors.joining(","));
        String sageCode = order.establishment.map(e -> e.establishment.name).orElse(""); // TODO sageCode or legacyCode ?
        for (String column : PaymentsCsvFileUtils.getColumns()) {
            if (column.equals("num ligne")) {
                values.put(column, number.toString());
            } else if (column.equals("id entite")) {
                values.put(column, "15");
            } else if (column.equals("entite")) {
                values.put(column, "AGENCE NORMANDIE");
            } else if (column.equals("date enregist")) {
                values.put(column, dateFormat.format(payment.date));
            } else if (column.equals("numero mission")) {
                values.put(column, mission.getName());
            } else if (column.equals("montant reglement")) {
                values.put(column, df.format(payment.value));
            } else if (column.equals("mode reglement")) {
                values.put(column, "virement");
            } else if (column.equals("code offre")) {
                values.put(column, codeOffre);
            } else if (column.equals("expert")) {
                Expert expert = mission.getPlanning().getExpert();
                values.put(column, expert.firstname + " " + expert.lastname);
            } else if (column.equals("commercial")) {
                values.put(column, commercial.first_name + " " + commercial.last_name);
            } else if (column.equals("id societe")) {
                values.put(column, sageCode);
            } else if (column.equals("date rdv")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("categorie facturee")) {
                values.put(column, "client final en gestion gcl");
            } else if (column.equals("origine")) {
                values.put(column, "Calypso");
            } else if (column.equals("taux tva")) {
                values.put(column, "20");
            } else if (column.equals("CC GC")) {
                values.put(column, "GC");
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public PaymentCsvLine(HashMap<String, String> values) {
        super(values);
    }

}
