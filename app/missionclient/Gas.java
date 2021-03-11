package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Gas extends PrestationResult {
    public final static String TYPE = "diagnostic_gaz";
    public final static String LABEL = "Gaz";
    public final String missionType;
    public final Optional<String> prestationId;
    public final Optional<Boolean> isPresent;
    public final Optional<String> gasType;
    public final Optional<String> anomalyType1;
    public final Optional<String> anomalyType2;
    public final Optional<String> anomalyType3;
    public final Optional<String> anomalyImmediateDanger;
    public final Optional<String> anomaly32C;
    public final Optional<String> DGINumber;

    public Gas(
            String missionType,
            Optional<String> prestationId,
            Optional<Boolean> isPresent,
            Optional<String> gasType,
            Optional<String> anomalyType1,
            Optional<String> anomalyType2,
            Optional<String> anomalyType3,
            Optional<String> anomalyImmediateDanger,
            Optional<String> anomaly32C,
            Optional<String> dgiNumber
    ) {
        super(TYPE, LABEL);

        this.missionType = missionType;
        this.prestationId = prestationId;
        this.isPresent = isPresent;
        this.gasType = gasType;
        this.anomalyType1 = anomalyType1;
        this.anomalyType2 = anomalyType2;
        this.anomalyType3 = anomalyType3;
        this.anomalyImmediateDanger = anomalyImmediateDanger;
        this.anomaly32C = anomaly32C;
        this.DGINumber = dgiNumber;
    }

    @JsonCreator
    public Gas(
            @JsonProperty("missionType") String missionType,
            @JsonProperty("prestationId") String prestationId,
            @JsonProperty("isPresent") Boolean isPresent,
            @JsonProperty("gasType") String gasType,
            @JsonProperty("anomalyType1") String anomalyType1,
            @JsonProperty("anomalyType2") String anomalyType2,
            @JsonProperty("anomalyType3") String anomalyType3,
            @JsonProperty("anomalyImmediateDanger") String anomalyImmediateDanger,
            @JsonProperty("anomaly32C") String anomaly32C,
            @JsonProperty("dgiNumber") String dgiNumber
    ) {
        super(TYPE, LABEL);

        this.missionType = missionType;
        this.prestationId = Optional.ofNullable(prestationId);
        this.isPresent = Optional.ofNullable(isPresent);
        this.gasType = Optional.ofNullable(gasType);
        this.anomalyType1 = Optional.ofNullable(anomalyType1);
        this.anomalyType2 = Optional.ofNullable(anomalyType2);
        this.anomalyType3 = Optional.ofNullable(anomalyType3);
        this.anomalyImmediateDanger = Optional.ofNullable(anomalyImmediateDanger);
        this.anomaly32C = Optional.ofNullable(anomaly32C);
        this.DGINumber = Optional.ofNullable(dgiNumber);
    }

}
