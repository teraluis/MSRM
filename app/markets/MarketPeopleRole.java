package markets;

public enum MarketPeopleRole {
    KEY("Contact principal"),
    ACCOUNTING("Contact comptable"),
    BILLING("Contact facturation"),
    PURCHASER("Donneur d'ordre / Apporteur d'affaire"),
    REPORT("Contact envoi de rapport");

    private final String value;

    MarketPeopleRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
