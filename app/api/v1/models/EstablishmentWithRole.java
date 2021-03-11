package api.v1.models;

public class EstablishmentWithRole {

    public Establishment establishment;
    public String role;

    public EstablishmentWithRole(Establishment establishment, String role) {
        this.establishment = establishment;
        this.role = role;
    }

    public static EstablishmentWithRole serialize(Establishment establishment, String role) {
        return new EstablishmentWithRole(establishment, role);
    }
}
