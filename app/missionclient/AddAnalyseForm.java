package missionclient;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class AddAnalyseForm {
    protected final Optional<String> orderLineId;
    protected final String typeId;

    public AddAnalyseForm(@JsonProperty("orderLineId") String orderLineId,
                          @JsonProperty("typeId") String typeId) {
        this.orderLineId = Optional.ofNullable(orderLineId);
        this.typeId = typeId;
    }
}
