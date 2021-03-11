package utils.Sage1000Export;

import api.v1.models.Establishment;
import api.v1.models.FactureLigne;
import api.v1.models.User;
import core.CsvLine;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;

public class Sage1000BillCsvLine extends CsvLine {

    private static BigDecimal calculateTotalWithoutTax(List<FactureLigne> lignes) {
        return lignes.stream().map(l -> {
            BigDecimal priceWithoutTaxNorDiscount = l.price.multiply(new BigDecimal(l.quantity));
            return priceWithoutTaxNorDiscount.subtract(priceWithoutTaxNorDiscount.multiply(l.discount.divide(new BigDecimal(100))));
        }).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public static HashMap<String, String> generateSage1000BillLine(final String billName, final List<FactureLigne> lignes, final Establishment client, final Optional<Establishment> billed, final Boolean isCreditNote, final User commercial) {
        HashMap<String, String> values = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        for (String column : Sage1000BillCsvUtils.getColumns()) {
            if (column.equals("code_societe")) {
                values.put(column, "01-ADXG");
            } else if (column.equals("code_journal")) {
                values.put(column, "VEC");
            } else if (column.equals("date_ecriture")) {
                values.put(column, format.format(new Date()));
            } else if (column.equals("type_piece")) {
                if (!isCreditNote) {
                    values.put(column, "FC");
                } else {
                    values.put(column, "AC");
                }
            } else if (column.equals("compte_general")) {
                values.put(column, "41100001"); // TODO : check if factor and change value
            } else if (column.equals("type_ecriture")) {
                values.put(column, "X");
            } else if (column.equals("code_tiers_facture")) {
                values.put(column, billed.map(b -> b.sageCode).orElse(client.sageCode)); // TODO : individual use case
            } else if (column.equals("ref_piece")) {
                values.put(column, billName);
            } else if (column.equals("libelle")) {
                String clientName = client.name;
                values.put(column, billName + " " + clientName);
            } else if (column.equals("sens")) {
                if (!isCreditNote) {
                    values.put(column, "D");
                } else {
                    values.put(column, "C");
                }
            } else if (column.equals("montant")) {
                BigDecimal total = lignes.stream().map(l -> l.total).reduce(BigDecimal.ZERO, BigDecimal::add);
                values.put(column, df.format(total));
            } else if (column.equals("mode_reglement")) {
                // TODO : add to and take from client
                values.put(column, "VIR");
            } else if (column.equals("echeance")) {
                // TODO : add echeance to market or client ?
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.DAY_OF_MONTH, 30);
                values.put(column, format.format(cal.getTime()));
            } else if (column.equals("code_commercial") && commercial.registration_number.isPresent()) {
                values.put(column, commercial.registration_number.get());
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public static HashMap<String, String> generateSage1000TaxLine(final String billName, final BigDecimal value, final Establishment client, final Boolean isCreditNote, final String codeTVA, final User commercial) {
        HashMap<String, String> values = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        for (String column : Sage1000BillCsvUtils.getColumns()) {
            if (column.equals("code_societe")) {
                values.put(column, "01-ADXG");
            } else if (column.equals("code_journal")) {
                values.put(column, "VEC");
            } else if (column.equals("date_ecriture")) {
                values.put(column, format.format(new Date()));
            } else if (column.equals("type_piece")) {
                if (!isCreditNote) {
                    values.put(column, "FC");
                } else {
                    values.put(column, "AC");
                }
            } else if (column.equals("compte_general")) {
                values.put(column, codeTVA);
            } else if (column.equals("type_ecriture")) {
                values.put(column, "G");
            } else if (column.equals("ref_piece")) {
                values.put(column, billName);
            } else if (column.equals("libelle")) {
                String clientName = client.name;
                values.put(column, billName + " " + clientName);
            } else if (column.equals("sens")) {
                if (!isCreditNote) {
                    values.put(column, "C");
                } else {
                    values.put(column, "D");
                }
            } else if (column.equals("montant")) {
                values.put(column, df.format(value));
            } else if (column.equals("code_commercial") && commercial.registration_number.isPresent()) {
                values.put(column, commercial.registration_number.get());
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public static HashMap<String, String> generateSage1000PrestationLine(final String billName, final String generalAccount, final List<FactureLigne> lignes, final Establishment client, final Boolean isCreditNote, final String profileTVA, final User commercial) {
        HashMap<String, String> values = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        for (String column : Sage1000BillCsvUtils.getColumns()) {
            if (column.equals("code_societe")) {
                values.put(column, "01-ADXG");
            } else if (column.equals("code_journal")) {
                values.put(column, "VEC");
            } else if (column.equals("date_ecriture")) {
                values.put(column, format.format(new Date()));
            } else if (column.equals("type_piece")) {
                if (!isCreditNote) {
                    values.put(column, "FC");
                } else {
                    values.put(column, "AC");
                }
            } else if (column.equals("compte_general")) {
                values.put(column, generalAccount); // TODO : change number when done by external expert
            } else if (column.equals("type_ecriture")) {
                values.put(column, "G");
            } else if (column.equals("ref_piece")) {
                values.put(column, billName);
            } else if (column.equals("libelle")) {
                String clientName = client.name;
                values.put(column, billName + " " + clientName);
            } else if (column.equals("sens")) {
                if (!isCreditNote) {
                    values.put(column, "C");
                } else {
                    values.put(column, "D");
                }
            } else if (column.equals("montant")) {
                BigDecimal total = calculateTotalWithoutTax(lignes);
                values.put(column, df.format(total));
            } else if (column.equals("profil_tva")) {
                values.put(column, profileTVA);
            } else if (column.equals("code_commercial") && commercial.registration_number.isPresent()) {
                values.put(column, commercial.registration_number.get());
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public static HashMap<String, String> generateSage1000AxeLine(final String billName, final String generalAccount, final List<FactureLigne> lignes, final Establishment client, final String agency, final String expert, final Boolean isCreditNote, final User commercial, final BigDecimal factor) {
        HashMap<String, String> values = new HashMap<>();
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        for (String column : Sage1000BillCsvUtils.getColumns()) {
            if (column.equals("code_societe")) {
                values.put(column, "01-ADXG");
            } else if (column.equals("code_journal")) {
                values.put(column, "VEC");
            } else if (column.equals("date_ecriture")) {
                values.put(column, format.format(new Date()));
            } else if (column.equals("type_piece")) {
                if (!isCreditNote) {
                    values.put(column, "FC");
                } else {
                    values.put(column, "AC");
                }
            } else if (column.equals("compte_general")) {
                values.put(column, generalAccount); // TODO : change number when done by external expert
            } else if (column.equals("type_ecriture")) {
                values.put(column, "A");
            } else if (column.equals("ref_piece")) {
                values.put(column, billName);
            } else if (column.equals("libelle")) {
                String clientName = client.name;
                values.put(column, billName + " " + clientName);
            } else if (column.equals("sens")) {
                if (!isCreditNote) {
                    values.put(column, "C");
                } else {
                    values.put(column, "D");
                }
            } else if (column.equals("montant")) {
                BigDecimal total = calculateTotalWithoutTax(lignes);
                BigDecimal finalValue = total.multiply(factor);
                values.put(column, df.format(finalValue));
            } else if (column.equals("axe")) {
                values.put(column, "Axe 1"); // TODO : add more axis
            } else if (column.equals("section")) {
                values.put(column, agency + "/" + expert);
            } else if (column.equals("code_commercial") && commercial.registration_number.isPresent()) {
                values.put(column, commercial.registration_number.get());
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public Sage1000BillCsvLine(HashMap<String, String> values) {
        super(values);
    }
}
