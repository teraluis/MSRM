package api.v1.models;

public class AdnParameters {

    public final String uuid;
    public final String name;
    public final Integer adnId;

    public AdnParameters(final String uuid, final String name, final Integer adnId) {
        this.uuid = uuid;
        this.name = name;
        this.adnId = adnId;
    }

    public static AdnParameters serialize(entities.AdnParameters adnParameters, Entity entity) {
        return new AdnParameters(entity.uuid, entity.name, adnParameters.adnId);
    }
}
