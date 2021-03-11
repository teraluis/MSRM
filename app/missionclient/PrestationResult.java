package missionclient;

public abstract class PrestationResult {

    public PrestationResult(final String type, final String label) {
        this.type = type;
        this.label = label;
    }

    public final String type;
    public final String label;
}