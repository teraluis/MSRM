package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AddPrestationForm {
    public final Optional<String> status;
    public final Optional<String> order;
    public final Optional<String> mission;
    public final Optional<String> technicalActId;
    public final Optional<String> comment;
    public final Optional<String> resultId;
    public final Optional<String> diagnostician;
    public final Optional<String> estate;
    public final Optional<String> orderLine;
    public final Optional<AddAnalyseForm> analyse;
    public final Optional<String> estateType;
    public final Optional<String> workDescription;
    public final Optional<String> targetId;

    @JsonCreator
    public AddPrestationForm(
            @JsonProperty("status") String status,
            @JsonProperty("order") String order,
            @JsonProperty("mission") String mission,
            @JsonProperty("technicalActId") String technicalAct,
            @JsonProperty("comment") String comment,
            @JsonProperty("resultId") String resultId,
            @JsonProperty("diagnostician") String diagnostician,
            @JsonProperty("estate") String estate,
            @JsonProperty("orderLine") String orderLine,
            @JsonProperty("analyseId") AddAnalyseForm analyse,
            @JsonProperty("estateType") String estateType,
            @JsonProperty("workDescription") String workDescription,
            @JsonProperty("targetId") String targetId) {
        this.status = Optional.ofNullable(status);
        this.order = Optional.ofNullable(order);
        this.mission = Optional.ofNullable(mission);
        this.technicalActId = Optional.ofNullable(technicalAct);
        this.comment = Optional.ofNullable(comment);
        this.resultId = Optional.ofNullable(resultId);
        this.diagnostician = Optional.ofNullable(diagnostician);
        this.estate = Optional.ofNullable(estate);
        this.orderLine = Optional.ofNullable(orderLine);
        this.analyse = Optional.ofNullable(analyse);
        this.estateType = Optional.ofNullable(estateType);
        this.workDescription = Optional.ofNullable(workDescription);
        this.targetId = Optional.ofNullable(targetId);
    }
}
