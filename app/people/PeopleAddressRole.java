package people;

public enum PeopleAddressRole {
    MAIN("Adresse principale"),
    BILLING("Adresse de facturation"),
    DELIVERY("Adresse de livraison"),
    OTHER("Adresse autre");

    private final String value;

    PeopleAddressRole(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
