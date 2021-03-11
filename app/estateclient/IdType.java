package estateclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdType {
    public final String id;
    public final String type;
    public final Boolean deleted;

    public IdType(
            @JsonProperty("id") String id,
            @JsonProperty("type") String type,
            @JsonProperty("deleted") Boolean deleted
    ) {
        this.id = id;
        this.type = type;
        this.deleted = deleted;
    }
}
