package establishments;

public enum EstablishmentPeopleRole {
    MAIN("Principal / Financier / Comptable"),
    NEGOTIATOR("Négociateur immobilier"),
    MANAGER("Gestionnaire / Chargé de secteur / patrimoine"),
    ADMINISTRATIVE("Contact / Valideur administratif"),
    PURCHASER("Donneur d'ordre / Apporteur d'affaire"),
    TECHNICAL("Responsable / Valideur technique"),
    REPORT("Envoi de rapport"),
    OTHER("Autre");

    private final String value;

    EstablishmentPeopleRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
