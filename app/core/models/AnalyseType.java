package core.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class AnalyseType {
    public final String uuid;
    public final String label;
    public final Optional<String> description;

    public AnalyseType(@JsonProperty("uuid") String uuid,
                @JsonProperty("label") String label,
                @JsonProperty("description") String description) {
        this.uuid = uuid;
        this.label = label;
        this.description = Optional.ofNullable(description);
    }

    public static String DEFAULT_ID = "analysetype-9ecc55ca-c86a-49fc-ae40-4ea1f98d9a32";
}
