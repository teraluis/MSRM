package utils.VariablesExport;

public class RecoveryCsvFileUtils {
    private final static String[] columns = new String[]{
            "id entite",
            "region",
            "date facture",
            "numero mission",
            "numero marche",
            "Expert Facture",
            "Total TTC",
            "Restant du",
            "total TTC prlv",
            "statut commande Calypso",
            "commentaire Calypso",
            "numero Facture",
            "statut Facture",
            "commercial",
            "code offre",
            "validateur societe",
            "validateur nom",
            "validateur prenom",
            "validateur adresse1",
            "validateur adresse2",
            "validateur cp",
            "validateur ville",
            "validateur telephone",
            "validateur email",
            "proprietaire nom",
            "proprietaire prenom",
            "bien adresse1",
            "bien adresse2",
            "bien cp",
            "bien ville",
            "statut paiement",
            "code client facturation",
            "date envoi",
            "date mission",
            "ref client",
            "id mission",
            "date echeance",
            "Prescripteur",
            "prsc nom",
            "prsc adresse 1",
            "prsc adresse 2",
            "prsc CP",
            "prsc Ville",
            "prsc id"
    };

    public static String[] getColumns() {
        return columns;
    }

}
