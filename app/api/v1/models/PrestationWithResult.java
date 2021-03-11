package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.JsonNode;
import core.models.Prestation;

import java.util.Optional;

public class PrestationWithResult {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Prestation prestation;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<JsonNode> result;


    public PrestationWithResult(final Prestation prestation, final Optional<JsonNode> result) {
        this.prestation = prestation;
        this.result = result;
    }
}
