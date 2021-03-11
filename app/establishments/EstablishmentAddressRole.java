package establishments;

public enum EstablishmentAddressRole {
    MAIN("Adresse principale"),
    BILLING("Adresse de facturation"),
    DELIVERY("Adresse de livraison"),
    OTHER("Adresse autre");

    private final String value;

    EstablishmentAddressRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
