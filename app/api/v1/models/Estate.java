package api.v1.models;

public class Estate {
    public final String estateId;
    public final String addressId;

    public Estate(final String estateId, final String addressId) {
        this.estateId = estateId;
        this.addressId = addressId;
    }
}
