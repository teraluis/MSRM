package utils.VariablesExport;

public class InProgressCsvFileUtils {
    private final static String[] columns = new String[]{
            "id entite",
            "numero mission",
            "date enregist",
            "date rdv",
            "do societe",
            "expert",
            "statut mission",
            "rapport prelevt amiante nbre"
    };

    public static String[] getColumns() {
        return columns;
    }

}
