package markets;

import java.util.Objects;

public class SimpleMarketEstablishment {
    public String establishment;
    public String role;

    public SimpleMarketEstablishment(
            String establishment,
            String role
    ) {
        this.establishment = establishment;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMarketEstablishment)) return false;
        SimpleMarketEstablishment that = (SimpleMarketEstablishment) o;
        return establishment.equals(that.establishment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(establishment);
    }
}
