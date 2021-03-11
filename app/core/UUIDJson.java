package core;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UUIDJson {

    public final String uuid;

    public UUIDJson(@JsonProperty("uuid") String uuid) {
        this.uuid = uuid;
    }
}
