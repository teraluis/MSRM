package estateclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Annex {
    public final String id;
    public final Optional<String> floor;
    public final Optional<Double> area;
    public final Boolean isCommonArea;
    public final Optional<IdType> annexType;
    public final Optional<String> customAnnexType;
    public final Optional<String> annexReference;
    public final Boolean deleted;

    public Annex(
            @JsonProperty("id") String id,
            @JsonProperty("floor") String floor,
            @JsonProperty("area") Double area,
            @JsonProperty("isCommonArea") Boolean isCommonArea,
            @JsonProperty("annexType") IdType annexType,
            @JsonProperty("customAnnexType") String customAnnexType,
            @JsonProperty("annexReference") String annexReference,
            @JsonProperty("deleted") Boolean deleted
    ) {
        this.id = id;
        this.floor = Optional.ofNullable(floor);
        this.area = Optional.ofNullable(area);
        this.isCommonArea = isCommonArea;
        this.annexType = Optional.ofNullable(annexType);
        this.customAnnexType = Optional.ofNullable(customAnnexType);
        this.annexReference = Optional.ofNullable(annexReference);
        this.deleted = deleted;
    }
}
