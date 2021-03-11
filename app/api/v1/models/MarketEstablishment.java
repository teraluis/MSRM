package api.v1.models;

public class MarketEstablishment {
    public final Establishment establishment;
    public final String role;

    public MarketEstablishment(Establishment establishment, String role) {
        this.establishment = establishment;
        this.role = role;
    }

    public static MarketEstablishment serialize(Establishment establishment, String role) {
        return new MarketEstablishment(establishment, role);
    }
}
