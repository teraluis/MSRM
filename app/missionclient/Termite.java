package missionclient;

import java.util.Optional;

public class Termite extends PrestationResult {
    public final static String TYPE = "diagnostic_termite";
    public final static String LABEL = "Termite";
    public final Optional<Boolean> isPresent;
    public final Optional<String> prestationId;
    public final String missionType;

    public Termite(String missionType, Optional<String> prestationId, Optional<Boolean> isPresent) {
        super(TYPE, LABEL);

        this.isPresent = isPresent;
        this.prestationId = prestationId;
        this.missionType = missionType;
    }
}
