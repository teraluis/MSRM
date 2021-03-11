package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.Comment;
import core.models.Prestation;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SettledIntervention extends CreatedIntervention {
    protected final InterventionParams parameters;

    @JsonCreator
    public SettledIntervention(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createDate") Date createDate,
            @JsonProperty("estateAddress") String estateAddress,
            @JsonProperty("prestations") List<Prestation> prestations,
            @JsonProperty("parameters") InterventionParams parameters,
            @JsonProperty("comments") List<Comment> comments) {
        super(id, name, status, createdBy, createDate, estateAddress, prestations, comments);
        this.parameters = parameters;
    }

    public InterventionParams getParameters() {
        return parameters;
    }
}
