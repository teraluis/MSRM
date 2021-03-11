package api.v1.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.Prestation;

import java.util.List;

public class SetOrderLines {

    public final List<Prestation> oldPrestations;
    public final List<OrderLineWithPrestations> newPrestations;
    public final List<OrderLine> analyseOrderLines;

    public SetOrderLines(
            @JsonProperty("oldPrestations") List<Prestation> oldPrestations,
            @JsonProperty("newPrestations") List<OrderLineWithPrestations> newPrestations,
            @JsonProperty("analyseOrderLines") List<OrderLine> analyseOrderLines) {
        this.oldPrestations = oldPrestations;
        this.newPrestations = newPrestations;
        this.analyseOrderLines = analyseOrderLines;
    }
}
