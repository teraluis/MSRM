package markets;

public enum MarketUserRole {
    COMMERCIAL("Commercial Référent"),
    TECHNICAL("RT Référent"),
    ADMINISTRATIVE("Responsable administratif");

    private final String value;

    MarketUserRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
