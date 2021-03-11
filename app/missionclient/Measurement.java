package missionclient;

import java.util.Optional;

public class Measurement extends PrestationResult {
    public final static String TYPE = "mesurage";
    public final static String LABEL = "Mesurage";

    public final String measurementType;
    public final Optional<String> prestationId;
    public final Optional<String> carrezSurface;
    public final Optional<String> livingSpace;

    public Measurement(final String measurementType, Optional<String> prestationId, final Optional<String> carrezSurface, final Optional<String> livingSpace) {
        super(TYPE, LABEL);

        this.measurementType = measurementType;
        this.prestationId = prestationId;
        this.carrezSurface = carrezSurface;
        this.livingSpace = livingSpace;
    }
}
