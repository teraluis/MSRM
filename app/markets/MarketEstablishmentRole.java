package markets;

public enum MarketEstablishmentRole {
    CLIENT("Client"),
    ADMINISTRATIVE_VALIDATOR("Validateur administratif"),
    PURCHASER("Donneur d'ordre / Apporteur d'affaire");

    private final String value;

    MarketEstablishmentRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
