package missionclient.estates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Estate {
    public final String id;
    public final String adxReference;
    public final String name;
    public final Optional<String> estateReference;
    public final IdType estateType;
    public final Optional<String> customEstateType;
    public final String accountId; /*owner*/
    public final Integer state;
    public final List<Locality> localities;
    public final List<Attachment> attachments;
    public final Boolean deleted;

    public Estate(
            @JsonProperty("id") String id,
            @JsonProperty("adxReference") String adxReference,
            @JsonProperty("name") String name,
            @JsonProperty("estateReference") String estateReference,
            @JsonProperty("estateType") IdType estateType,
            @JsonProperty("customEstateType") String customEstateType,
            @JsonProperty("accountId") String accountId,
            @JsonProperty("state") Integer state,
            @JsonProperty("localities") List<Locality> localities,
            @JsonProperty("attachments") List<Attachment> attachments,
            @JsonProperty("deleted") Boolean deleted
    ) {
        this.id = id;
        this.adxReference = adxReference;
        this.name = name;
        this.estateReference = Optional.ofNullable(estateReference);
        this.estateType = estateType;
        this.customEstateType = Optional.ofNullable(customEstateType);
        this.accountId = accountId;
        this.state = state;
        this.localities = localities;
        this.attachments = attachments;
        this.deleted = deleted;
    }
}
