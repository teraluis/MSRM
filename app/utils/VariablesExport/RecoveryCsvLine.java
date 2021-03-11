package utils.VariablesExport;

import api.v1.models.Establishment;
import api.v1.models.Facture;
import api.v1.models.FactureLigne;
import api.v1.models.Paiement;
import api.v1.models.User;
import core.CsvLine;
import core.models.AddressWithRole;
import establishments.EstablishmentAddressRole;
import missionclient.Asbestos;
import missionclient.Expert;
import missionclient.interventions.DoneIntervention;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class RecoveryCsvLine extends CsvLine {

    public static HashMap<String, String> generateValuesFromBill(DoneIntervention mission, Facture bill, User commercial, List<Asbestos> asbestosResults, api.v1.models.Order order) {
        HashMap<String, String> values = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        DecimalFormat df = new DecimalFormat("#0.00", DecimalFormatSymbols.getInstance(Locale.US));
        BigDecimal total = BigDecimal.ZERO;
        List<String> offres = new ArrayList<>();
        for (FactureLigne line : bill.lignes) {
            total = total.add(line.total);
            offres.add(line.refadx);
        }

        BigDecimal restantDu = total;
        for (Paiement paiement: bill.paiements) {
            restantDu = restantDu.subtract(paiement.value);
        }

        String codeOffre = offres.stream().collect(Collectors.joining(","));
        String prescriberLabel;
        String prescriberName;
        String prescriberAddress1;
        String prescriberAddress2;
        String prescriberPostCode;
        String prescriberCity;
        String prescriberEmail;
        String prescriberNumber;
        String clientCode = order.establishment.map(e -> e.establishment.sageCode).orElse(""); // TODO sageCode or legacyCode ?
        Optional<AddressWithRole> firstAddress;
        if (order.establishment.isPresent()) {
            prescriberName = order.establishment.get().establishment.name;
            prescriberEmail = order.establishment.get().establishment.mail.orElse("");
            prescriberNumber = order.establishment.get().establishment.phone.orElse("");
            firstAddress = order.establishment.get().addresses.stream().anyMatch(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString()))
                    ? order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.BILLING.toString())).findAny()
                    : order.establishment.get().addresses.stream().filter(address -> address.role.equals(EstablishmentAddressRole.MAIN.toString())).findAny();
        } else {
            // TODO manage individuals
            prescriberName = "";
            prescriberEmail = "";
            prescriberNumber = "";
            firstAddress = Optional.of(new AddressWithRole(null, null));
        }
        // Must have an address
        prescriberAddress1 = firstAddress.get().address.address1.orElse("");
        prescriberAddress2 = firstAddress.get().address.address2.orElse("");
        prescriberPostCode = firstAddress.get().address.postCode.orElse("");
        prescriberCity = firstAddress.get().address.city.orElse("");
        prescriberLabel = prescriberName + " " + prescriberPostCode + " " + prescriberCity;

        String validatorName = "";
        String validatorCode = "";
        String validatorSurname = "";
        String validatorAddress1 = "";
        String validatorAddress2 = "";
        String validatorPostCode = "";
        String validatorCity = "";
        String validatorEmail = "";
        String validatorNumber = "";

        if (order.market.get().getValidatorEstablishment().isPresent()) {
            Establishment establishmentValidator = order.market.get().getValidatorEstablishment().get();
            validatorName = establishmentValidator.contact.get().firstname;
            validatorCode = establishmentValidator.name;
            validatorSurname = establishmentValidator.contact.get().lastname;
            validatorAddress1 = establishmentValidator.address.get().address1.get();
            validatorAddress2 = establishmentValidator.address.get().address2.orElse("");
            validatorPostCode = establishmentValidator.address.get().postCode.get();
            validatorCity = establishmentValidator.address.get().city.get();
            validatorEmail = establishmentValidator.mail.orElse("");
            validatorNumber = establishmentValidator.phone.orElse("");
        }

        for (String column : BillCsvFileUtils.getColumns()) {
            if (column.equals("id entite")) {
                if ( order.agency.isPresent() ) {
                    values.put(column, order.agency.get().code);
                } else {
                    values.put(column, "");
                }
            } else if (column.equals("date facture")) {
                values.put(column, dateFormat.format(bill.exportDate.get()));
            } else if (column.equals("numero mission")) {
                values.put(column, mission.getName());
            } else if (column.equals("Expert Facture")) {
                Expert expert = mission.getPlanning().getExpert();
                values.put(column, expert.firstname + " " + expert.lastname);
            } else if (column.equals("Total TTC")) {
                values.put(column, df.format(total));
            } else if (column.equals("Restant du")) {
                values.put(column, df.format(restantDu));
            } else if (column.equals("numero Facture")) {
                values.put(column, bill.name);
            } else if (column.equals("statut Facture")) {
                values.put(column, bill.status);
            } else if (column.equals("commercial")) {
                values.put(column, commercial.first_name + " " + commercial.last_name);
            } else if (column.equals("code offre")) {
                values.put(column, codeOffre);
            } else if (column.equals("date mission")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("date echeance")) {
                values.put(column, dateFormat.format(bill.deadline));
            } else if (column.equals("CC GC")) {
                values.put(column, "GC");
            } else if (column.equals("taux TVA")) {
                values.put(column, "20");
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
            } else if (column.equals("prsc telephone")) {
                values.put(column, prescriberNumber);
            } else if (column.equals("prsc email")) {
                values.put(column, prescriberEmail);
            } else if (column.equals("validateur societe")) {
                values.put(column, validatorCode);
            } else if (column.equals("validateur nom")) {
                values.put(column, validatorSurname);
            } else if (column.equals("validateur prenom")) {
                values.put(column, validatorName);
            } else if (column.equals("validateur adresse1")) {
                values.put(column, validatorAddress1);
            } else if (column.equals("validateur adresse2")) {
                values.put(column, validatorAddress2);
            } else if (column.equals("validateur cp")) {
                values.put(column, validatorPostCode);
            } else if (column.equals("validateur ville")) {
                values.put(column, validatorCity);
            } else if (column.equals("validateur telephone")) {
                values.put(column, validatorNumber);
            } else if (column.equals("validateur email")) {
                values.put(column, validatorEmail);
            } else if (column.equals("prsc id")) {
                values.put(column, clientCode);
            } else if (column.equals("statut commande Calypso")) {
                values.put(column, order.status);
            } else if (column.equals("commentaire Calypso")) {
                values.put(column, order.commentary.get());
            } else if (column.equals("date intervention")) {
                values.put(column, dateFormat.format(mission.getPlanning().getStartTime()));
            } else if (column.equals("region")) {
                if ( order.agency.isPresent() ) {
                    values.put(column, order.agency.get().name);
                } else {
                    values.put(column, "");
                }
            } else if (column.equals("numero marche")) {
                if ( order.market.isPresent()) {
                    values.put(column, order.market.get().marketNumber);
                } else {
                    values.put(column, "");
                }
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public RecoveryCsvLine(HashMap<String, String> values) {
        super(values);
    }

}
