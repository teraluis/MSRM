package missionclient;

import java.util.Optional;

public class Lead extends PrestationResult {
    public final static String TYPE = "diagnostic_plomb";
    public final static String LABEL = "Plomb";

    public final String missionType;
    public final Optional<String> prestationId;
    public final Optional<Boolean> isPresent;
    public final Optional<Float> diagnosticNegativeUnit;
    public final Optional<Float> diagnosticDamagedPositiveUnit;
    public final Optional<Float> diagnosticCleanPositiveUnit;
    public final Optional<Float> diagnosticWorkingPositiveUnit;

    public Lead(String missionType, Optional<String> prestationId, Optional<Boolean> isPresent, Optional<Float> diagnosticNegativeUnit, Optional<Float> diagnosticDamagedPositiveUnit, Optional<Float> diagnosticCleanPositiveUnit, Optional<Float> diagnosticWorkingPositiveUnit) {
        super(TYPE, LABEL);

        this.missionType = missionType;
        this.prestationId = prestationId;
        this.isPresent = isPresent;
        this.diagnosticNegativeUnit = diagnosticNegativeUnit;
        this.diagnosticDamagedPositiveUnit = diagnosticDamagedPositiveUnit;
        this.diagnosticCleanPositiveUnit = diagnosticCleanPositiveUnit;
        this.diagnosticWorkingPositiveUnit = diagnosticWorkingPositiveUnit;
    }
}
