package utils.Sage1000Export;

public class Sage1000ClientCsvUtils {

    private final static String[] columns = new String[]{
            "type_tiers",
            "code_tiers",
            "type_personne",
            "raison_sociale",
            "code_siret",
            "tva_intracom",
            "nom",
            "prenom",
            "adresse_facturation_1",
            "adresse_facturation_2",
            "adresse_facturation_3",
            "code_postal",
            "ville",
            "pays",
            "telephone_1",
            "telephone_2",
            "mail",
            "iban",
            "non_banque",
            "bic",
            "domiciliation",
            "valideur_administrateur",
            "responsable_compte",
            "mode_reglement",
            "mode_echeancement",
            "compte_general_privilegie",
            "date_debut_factpr",
            "date_fin_factor",
            "categorie_client"
    };

    public static String[] getColumns() {
        return columns;
    }

}
