package core.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddEstateForTest {

    public final String estate;
    public final String targetId;


    public AddEstateForTest(@JsonProperty("estate") String estate,
                            @JsonProperty("targetId") String targetId) {
        this.estate = estate;
        this.targetId = targetId;
    }
}
