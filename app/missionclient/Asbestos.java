package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Asbestos extends PrestationResult {
    public final static String TYPE = "diagnostic_amiante";
    public final static String LABEL = "Amiante";
    public final String missionType;
    public final Optional<String> prestationId;
    public final Optional<Boolean> isPresent;
    public final Optional<Integer> analyseCount;
    public final Optional<Integer> positiveAnalyseCount;
    public final Optional<Integer> analysesLabo;
    public final Optional<Integer> analysesLaboPositive;
    public final Optional<Integer> analysesLaboNegative;
    public final Optional<Integer> n1;
    public final Optional<Integer> n2;
    public final Optional<Integer> n3;
    public final Optional<Integer> ep;
    public final Optional<Integer> ac1;
    public final Optional<Integer> ac2;
    public final Optional<String> workDescription;

    public Asbestos(
            String missionType,
            Optional<String> prestationId,
            Optional<Boolean> isPresent,
            Optional<Integer> analyseCount,
            Optional<Integer> positiveAnalyseCount,
            Optional<Integer> analysesLabo,
            Optional<Integer> analysesLaboPositive,
            Optional<Integer> analysesLaboNegative,
            Optional<Integer> n1,
            Optional<Integer> n2,
            Optional<Integer> n3,
            Optional<Integer> ep,
            Optional<Integer> ac1,
            Optional<Integer> ac2,
            Optional<String> workDescription) {
        super(TYPE, LABEL);
        this.missionType = missionType;
        this.prestationId = prestationId;
        this.isPresent = isPresent;
        this.analyseCount = analyseCount;
        this.positiveAnalyseCount = positiveAnalyseCount;
        this.analysesLabo = analysesLabo;
        this.analysesLaboPositive = analysesLaboPositive;
        this.analysesLaboNegative = analysesLaboNegative;
        this.n1 = n1;
        this.n2 = n2;
        this.n3 = n3;
        this.ep = ep;
        this.ac1 = ac1;
        this.ac2 = ac2;
        this.workDescription = workDescription;
    }

    @JsonCreator
    public Asbestos(
            @JsonProperty("missionType") String missionType,
            @JsonProperty("prestationId") String prestationId,
            @JsonProperty("isPresent") Boolean isPresent,
            @JsonProperty("analyseCount") Integer analyseCount,
            @JsonProperty("positiveAnalyseCount") Integer positiveAnalyseCount,
            @JsonProperty("analysesLabo") Integer analysesLabo,
            @JsonProperty("analysesLaboPositive") Integer analysesLaboPositive,
            @JsonProperty("analysesLaboNegative") Integer analysesLaboNegative,
            @JsonProperty("n1") Integer n1,
            @JsonProperty("n2") Integer n2,
            @JsonProperty("n3") Integer n3,
            @JsonProperty("ep") Integer ep,
            @JsonProperty("ac1") Integer ac1,
            @JsonProperty("ac2") Integer ac2,
            @JsonProperty("workDescription") String workDescription) {
        super(TYPE, LABEL);
        this.missionType = missionType;
        this.prestationId = Optional.ofNullable(prestationId);
        this.isPresent = Optional.ofNullable(isPresent);
        this.analyseCount = Optional.ofNullable(analyseCount);
        this.positiveAnalyseCount = Optional.ofNullable(positiveAnalyseCount);
        this.analysesLabo = Optional.ofNullable(analysesLabo);
        this.analysesLaboPositive = Optional.ofNullable(analysesLaboPositive);
        this.analysesLaboNegative = Optional.ofNullable(analysesLaboNegative);
        this.n1 = Optional.ofNullable(n1);
        this.n2 = Optional.ofNullable(n2);
        this.n3 = Optional.ofNullable(n3);
        this.ep = Optional.ofNullable(ep);
        this.ac1 = Optional.ofNullable(ac1);
        this.ac2 = Optional.ofNullable(ac2);
        this.workDescription = Optional.ofNullable(workDescription);
    }
}
