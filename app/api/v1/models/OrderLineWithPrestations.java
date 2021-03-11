package api.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.Prestation;

import java.util.List;

public class OrderLineWithPrestations {

    public final OrderLine orderLine;
    public final List<Prestation> prestations;

    public OrderLineWithPrestations(
            @JsonProperty("orderLine") OrderLine orderLine,
            @JsonProperty("prestations") List<Prestation> prestations) {
        this.orderLine = orderLine;
        this.prestations = prestations;
    }
}
