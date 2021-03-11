package establishments;

public enum EstablishmentDelegateRole {
    PAYER("Payeur"),
    BILLED("Facturé"),
    MANAGER("Gestionnaire / Chargé de secteur / patrimoine"),
    ADMINISTRATIVE("Contact / Valideur administratif"),
    PURCHASER("Donneur d'ordre / Apporteur d'affaire"),
    TECHNICAL("Responsable / Valideur technique"),
    REPORT("Envoi de rapport"),
    OTHER("Autre");

    private final String value;

    EstablishmentDelegateRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
