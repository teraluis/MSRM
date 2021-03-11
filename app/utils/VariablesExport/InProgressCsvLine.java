package utils.VariablesExport;

import api.v1.models.Facture;
import api.v1.models.FactureLigne;
import api.v1.models.Paiement;
import api.v1.models.User;
import core.CsvLine;
import missionclient.Asbestos;
import missionclient.Expert;
import missionclient.interventions.DoneIntervention;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class InProgressCsvLine extends CsvLine {

    public static HashMap<String, String> generateValuesFromBill(DoneIntervention mission, Facture bill, User commercial, List<Asbestos> asbestosResults, api.v1.models.Order order, Paiement payment) {
        HashMap<String, String> values = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));

        Integer nbPrelev = 0;
        for (Asbestos asbestos : asbestosResults) {
            nbPrelev = nbPrelev + asbestos.analyseCount.get();
        }

        List<String> offres = new ArrayList<>();
        for (FactureLigne line : bill.lignes) {
            offres.add(line.refadx);
        }
        String sageCode = order.establishment.map(e -> e.establishment.name).orElse(""); // TODO sageCode or legacyCode ?
        for (String column : InProgressCsvFileUtils.getColumns()) {
            if (column.equals("id entite")) {
                values.put(column, "15");
            } else if (column.equals("numero mission")) {
                values.put(column, mission.getName());
            } else if (column.equals("date enregist")) {
                values.put(column, dateFormat.format(payment.date));
            } else if (column.equals("date rdv")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("do societe")) {
                values.put(column, sageCode);
            } else if (column.equals("expert")) {
                Expert expert = mission.getPlanning().getExpert();
                values.put(column, expert.firstname + " " + expert.lastname);
            } else if (column.equals("statut mission")) {
                values.put(column, mission.getStatus());
            } else if (column.equals("rapport prelevt amiante nbre")) {
                values.put(column, df.format(nbPrelev));
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public InProgressCsvLine(HashMap<String, String> values) {
        super(values);
    }

}
