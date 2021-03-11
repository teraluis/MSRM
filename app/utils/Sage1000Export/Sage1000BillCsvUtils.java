package utils.Sage1000Export;

public class Sage1000BillCsvUtils {

    private final static String[] columns = new String[]{
            "code_societe",
            "code_journal",
            "date_ecriture",
            "type_piece",
            "compte_general",
            "type_ecriture",
            "code_tiers_facture",
            "code_particulier",
            "code_valideur",
            "ref_piece",
            "libelle",
            "sens",
            "montant",
            "axe",
            "section",
            "mode_reglement",
            "echeance",
            "profil_tva",
            "code_commercial"
    };

    public static String[] getColumns() {
        return columns;
    }

}
