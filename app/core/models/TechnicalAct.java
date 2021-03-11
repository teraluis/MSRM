package core.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TechnicalAct {

    public final String uuid;
    public final String name;
    public final String shortcut;
    public final Integer typeTVA;
    public final String codeTVA;
    public final String profilTVA;
    public final String schedulerId;
    public final Boolean hasAnalyse;
    public final Long created;
    public final String surfaceType;
    public final String productType;
    public final String offerCode;
    public final String comment;
    public final String businessCode;
    public final String jobRank;
    public final String jobExpertise;
    public final String description;
    public final Boolean active;
    public final Boolean web;
    @JsonCreator
    public TechnicalAct(
            @JsonProperty("uuid") final String uuid,
            @JsonProperty("name") final String name,
            @JsonProperty("shortcut") final String shortcut,
            @JsonProperty("typeTVA") final Integer typeTVA,
            @JsonProperty("codeTVA") final String codeTVA,
            @JsonProperty("profilTVA") final String profilTVA,
            @JsonProperty("schedulerId") final String schedulerId,
            @JsonProperty("hasAnalyse") final Boolean hasAnalyse,
            @JsonProperty("created") final Long created,
            @JsonProperty("surfaceType") final String surfaceType,
            @JsonProperty("productType") final String productType,
            @JsonProperty("offerCode") final String offerCode,
            @JsonProperty("comment") final String comment,
            @JsonProperty("businessCode") final String businessCode,
            @JsonProperty("jobRank") final String jobRank,
            @JsonProperty("jobExpertise") final String jobExpertise,
            @JsonProperty("description") final String description,
            @JsonProperty("active") final Boolean active,
            @JsonProperty("web") final Boolean web) {
        this.uuid = uuid;
        this.name = name;
        this.shortcut = shortcut;
        this.created = created;
        this.typeTVA = typeTVA;
        this.codeTVA = codeTVA;
        this.profilTVA = profilTVA;
        this.schedulerId = schedulerId;
        this.hasAnalyse = hasAnalyse;
        this.surfaceType = surfaceType;
        this.productType = productType;
        this.offerCode = offerCode;
        this.comment = comment;
        this.businessCode = businessCode;
        this.jobRank = jobRank;
        this.jobExpertise = jobExpertise;
        this.description = description;
        this.active = active;
        this.web = web;
    }
}

