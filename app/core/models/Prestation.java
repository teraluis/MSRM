package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Prestation {
    public final String uuid;
    public final Optional<String> status;
    public final Optional<String> order;
    public final Optional<String> mission;
    public final Optional<TechnicalAct> technicalAct;
    public final Optional<String> comment;
    public final Optional<String> workDescription;
    public final Optional<String> resultId;
    public final Optional<String> diagnostician;
    public final Optional<String> estate;
    public final Optional<String> targetId;
    public final Optional<String> orderLine;
    public final Optional<Analyse> analyse;
    public final Optional<String> estateType;
    public final List<String> billLines;

    @JsonCreator
    public Prestation(
            @JsonProperty("uuid") String uuid,
            @JsonProperty("status") String status,
            @JsonProperty("order") String order,
            @JsonProperty("mission") String mission,
            @JsonProperty("technicalAct") TechnicalAct technicalAct,
            @JsonProperty("comment") String comment,
            @JsonProperty("workDescription") String workDescription,
            @JsonProperty("resultId") String resultId,
            @JsonProperty("diagnostician") String diagnostician,
            @JsonProperty("estate") String estate,
            @JsonProperty("targetId") String targetId,
            @JsonProperty("orderLine") String orderLine,
            @JsonProperty("analyse") Analyse analyse,
            @JsonProperty("estateType") String estateType,
            @JsonProperty("billLines") List<String> billLines
    ) {
        this.uuid = uuid;
        this.status = Optional.ofNullable(status);
        this.order = Optional.ofNullable(order);
        this.mission = Optional.ofNullable(mission);
        this.technicalAct = Optional.ofNullable(technicalAct);
        this.comment = Optional.ofNullable(comment);
        this.workDescription = Optional.ofNullable(workDescription);
        this.resultId = Optional.ofNullable(resultId);
        this.diagnostician = Optional.ofNullable(diagnostician);
        this.estate = Optional.ofNullable(estate);
        this.targetId = Optional.ofNullable(targetId);
        this.orderLine = Optional.ofNullable(orderLine);
        this.analyse = Optional.ofNullable(analyse);
        this.estateType = Optional.ofNullable(estateType);
        this.billLines = billLines;
    }
}
