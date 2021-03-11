package missionclient.estates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Attachment {
    public final String id;
    public final String file;

    public Attachment(
            @JsonProperty("id") String id,
            @JsonProperty("file") String file) {
        this.id = id;
        this.file = file;
    }
}
