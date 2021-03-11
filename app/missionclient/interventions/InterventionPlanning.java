package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.Expert;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterventionPlanning {
    protected final long startTime;
    protected final int duration;
    protected final Expert expert;

    @JsonCreator
    public InterventionPlanning(
            @JsonProperty("startTime") long startTime,
            @JsonProperty("duration") int duration,
            @JsonProperty("expert") Expert expert
    ) {
        this.startTime = startTime;
        this.duration = duration;
        this.expert = expert;
    }

    public Date getStartTime() {
        return new Date(startTime);
    }

    public int getDuration() {
        return duration;
    }

    public Expert getExpert() {
        return expert;
    }
}
