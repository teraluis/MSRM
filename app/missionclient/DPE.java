package missionclient;

import java.util.Optional;

public class DPE extends PrestationResult {
    public final static String TYPE = "DPE";
    public final static String LABEL = "DPE";

    public final String missionType;
    public final Optional<String> prestationId;
    public final Optional<String> consumptionScore;
    public final Optional<String> consumptionValue;
    public final Optional<String> greenhouseGasScore;
    public final Optional<String> greenhouseGasValue;
    public final Optional<String> heatingSystemType;
    public final Optional<String> heatingSystemEnergy;
    public final Optional<String> hotWaterSystemEnergy;
    public final Optional<String> ADEME;

    public DPE(String missionType, Optional<String> prestationId, Optional<String> consumptionScore, Optional<String> consumptionValue, Optional<String> greenhouseGasScore, Optional<String> greenhouseGasValue, Optional<String> heatingSystemType, Optional<String> heatingSystemEnergy, Optional<String> hotWaterSystemEnergy, Optional<String> ademe) {
        super(TYPE, LABEL);
        this.missionType = missionType;
        this.prestationId = prestationId;
        this.consumptionScore = consumptionScore;
        this.consumptionValue = consumptionValue;
        this.greenhouseGasScore = greenhouseGasScore;
        this.greenhouseGasValue = greenhouseGasValue;
        this.heatingSystemType = heatingSystemType;
        this.heatingSystemEnergy = heatingSystemEnergy;
        this.hotWaterSystemEnergy = hotWaterSystemEnergy;
        ADEME = ademe;
    }
}
