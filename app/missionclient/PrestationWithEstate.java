package missionclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.estates.Estate;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PrestationWithEstate {
    public final String uuid;
    public final Optional<String> status;
    public final Optional<String> order;
    public final Optional<String> mission;
    public final Optional<String> technicalAct;
    public final Optional<String> comment;
    public final Optional<Boolean> unplanned;
    public final Optional<String> resultId;
    public final Optional<String> orderLine;
    public final Optional<Estate> estate;
    public final Optional<String> targetId;
    public final List<String> billLines;

    public PrestationWithEstate(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("status") String status,
            @JsonProperty("order") String order,
            @JsonProperty("mission") String mission,
            @JsonProperty("technicalAct") String technicalAct,
            @JsonProperty("comment") String comment,
            @JsonProperty("unplanned") Boolean unplanned,
            @JsonProperty("resultId") String resultId,
            @JsonProperty("orderLine") String orderLine,
            @JsonProperty("estate") Estate estate,
            @JsonProperty("targetId") String targetId,
            @JsonProperty("billLines") List<String> billLines) {
        this.uuid = uuid;
        this.status = Optional.ofNullable(status);
        this.order = Optional.ofNullable(order);
        this.mission = Optional.ofNullable(mission);
        this.technicalAct = Optional.ofNullable(technicalAct);
        this.comment = Optional.ofNullable(comment);
        this.unplanned = Optional.ofNullable(unplanned);
        this.resultId = Optional.ofNullable(resultId);
        this.orderLine = Optional.ofNullable(orderLine);
        this.billLines = billLines;
        this.estate = Optional.ofNullable(estate);
        this.targetId = Optional.ofNullable(targetId);
    }

}
