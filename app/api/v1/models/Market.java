package api.v1.models;

public class Market {

    public final String uuid;
    public final String name;
    public final String marketNumber;
    public final String facturationAnalysis;

    public Market(final String uuid, final String name, String marketNumber, final String facturationAnalysis) {
        this.uuid = uuid;
        this.name = name;
        this.marketNumber = marketNumber;
        this.facturationAnalysis = facturationAnalysis;
    }


}
