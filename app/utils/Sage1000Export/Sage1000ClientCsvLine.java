package utils.Sage1000Export;

import api.v1.models.FullEstablishment;
import core.CsvLine;
import core.models.Address;
import core.models.People;
import establishments.EstablishmentAddressRole;
import establishments.EstablishmentPeopleRole;

import java.util.HashMap;
import java.util.Optional;

public class Sage1000ClientCsvLine extends CsvLine {

    public static HashMap<String, String> generateValues(FullEstablishment establishment, Boolean validator) {
        HashMap<String, String> values = new HashMap<>();
        Optional<Address> billableAddress = establishment.addresses.stream().filter(a -> a.role.equals(EstablishmentAddressRole.BILLING.toString())).findAny().map(a -> a.address);
        if (!billableAddress.isPresent()) {
            billableAddress = establishment.addresses.stream().filter(a -> a.role.equals(EstablishmentAddressRole.MAIN.toString())).findAny().map(a -> a.address);
        }
        People contact = establishment.contacts.stream().filter(c -> c.role.equals(EstablishmentPeopleRole.MAIN.toString())).findAny().map(c -> c.people).orElseGet(() -> establishment.account.contact);
        for (String column : Sage1000ClientCsvUtils.getColumns()) {
            if (column.equals("type_tiers")) {
                if (validator) {
                    values.put(column, "Valideur");
                } else {
                    values.put(column, "Client");
                }
            } else if (column.equals("code_tiers")) {
                String prefix = validator ? "V" : "C";
                values.put(column, prefix + establishment.establishment.sageCode);
            } else if (column.equals("type_personne")) {
                values.put(column, "Morale"); // TODO : particulier
            } else if (column.equals("raison_sociale")) {
                values.put(column, establishment.establishment.corporateName);
            } else if (column.equals("code_siret")) {
                values.put(column, establishment.establishment.siret);
            } else if (column.equals("adresse_facturation_1") && billableAddress.isPresent() && billableAddress.get().address1.isPresent()) {
                values.put(column, billableAddress.get().address1.get());
            } else if (column.equals("adresse_facturation_2") && billableAddress.isPresent() && billableAddress.get().address2.isPresent()) {
                values.put(column, billableAddress.get().address2.get());
            } else if (column.equals("code_postal") && billableAddress.isPresent() && billableAddress.get().postCode.isPresent()) {
                values.put(column, billableAddress.get().postCode.get());
            } else if (column.equals("ville") && billableAddress.isPresent() && billableAddress.get().city.isPresent()) {
                values.put(column, billableAddress.get().city.get());
            } else if (column.equals("pays")) {
                values.put(column, "FRANCE");
            } else if (column.equals("telephone_1")) {
                String telephone = establishment.establishment.phone.orElseGet(() -> contact.workPhone.orElseGet(() -> contact.mobilePhone.orElse("")));
                values.put(column, telephone);
            } else if (column.equals("mail")) {
                String mail = establishment.establishment.mail.orElseGet(() -> contact.workMail.orElseGet(() -> contact.email.orElse("")));
                values.put(column, mail);
            } else if (column.equals("mode_reglement")) {
                values.put(column, "VIR");
            } else if (column.equals("mode_echeancement")) {
                values.put(column, "30JFM");
            } else if (column.equals("compte_general_privilegie")) {
                values.put(column, "41100001");
            } else if (column.equals("categorie_client")) {
                values.put(column, "Administration");
            } else {
                values.put(column, "");
            }
        }
        return values;
    }

    public Sage1000ClientCsvLine(HashMap<String, String> values) {
        super(values);
    }

}
