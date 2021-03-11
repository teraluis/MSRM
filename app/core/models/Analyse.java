package core.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Analyse {
    public final String uuid;
    public final Optional<String> orderLineId;
    public final AnalyseType type;

    public Analyse(@JsonProperty("uuid") String uuid,
                   @JsonProperty("orderLineId") String orderLineId,
                   @JsonProperty("type") AnalyseType type) {
        this.uuid = uuid;
        this.orderLineId = Optional.ofNullable(orderLineId);
        this.type = type;
    }
}
