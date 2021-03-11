package utils.VariablesExport;

public class PaymentsCsvFileUtils {

    private final static String[] columns = new String[]{
            "num ligne",
            "id entite",
            "entite",
            "date enregist",
            "numero mission",
            "date reglement",
            "montant reglement",
            "mode reglement",
            "destination bien",
            "code offre",
            "offre",
            "expert",
            "commercial",
            "id societe",
            "id societe prescripteur",
            "date rdv",
            "categorie facturee",
            "login utilisateur enregist",
            "nom utilisateur enregist",
            "prenom utilisateur enregist",
            "matricule utilisateur enregist",
            "origine",
            "date envoi",
            "id mission",
            "taux tva",
            "stratege facture",
            "Client Factor",
            "Date echeance",
            "CC GC",
            "brut HT"
    };

    public static String[] getColumns() {
        return columns;
    }
}
