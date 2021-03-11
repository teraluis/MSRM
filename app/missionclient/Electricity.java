package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Electricity extends PrestationResult {
    public final static String TYPE = "diagnostic_electricite";
    public final static String LABEL = "Electricité";
    public final String missionType;
    public final Optional<String> prestationId;
    public final Optional<Boolean> anomalyPresence;
    public final Optional<Boolean> electricityPresence;

    public Electricity(String missionType, Optional<String> prestationId, Optional<Boolean> anomalyPresence, Optional<Boolean> electricityPresence) {
        super(TYPE, LABEL);

        this.missionType = missionType;
        this.prestationId = prestationId;
        this.anomalyPresence = anomalyPresence;
        this.electricityPresence = electricityPresence;
    }

    @JsonCreator
    public Electricity(
            @JsonProperty("missionType") String missionType,
            @JsonProperty("prestationId") String prestationId,
            @JsonProperty("anomalyPresence") Boolean anomalyPresence,
            @JsonProperty("electricityPresence") Boolean electricityPresence
    ) {
        super(TYPE, LABEL);
        this.missionType = missionType;
        this.prestationId = Optional.ofNullable(prestationId);
        this.anomalyPresence = Optional.ofNullable(anomalyPresence);
        this.electricityPresence = Optional.ofNullable(electricityPresence);
    }
}
