package markets;

import api.v1.models.Establishment;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

public class MarketEstablishment {
    public Establishment establishment;
    public String role;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MarketEstablishment(
            @JsonProperty("establishment")
                    Establishment establishment,
            @JsonProperty("role")
            String role
    ) {
        this.establishment = establishment;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketEstablishment)) return false;
        MarketEstablishment that = (MarketEstablishment) o;
        return establishment.equals(that.establishment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(establishment);
    }
}
