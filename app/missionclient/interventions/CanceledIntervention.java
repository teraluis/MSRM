package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.Comment;
import core.models.Prestation;

import java.util.Date;
import java.util.List;

public class CanceledIntervention extends SettledIntervention {

    @JsonCreator
    public CanceledIntervention(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createDate") Date createDate,
            @JsonProperty("estateAddress") String estateAddress,
            @JsonProperty("prestations") List<Prestation> prestations,
            @JsonProperty("parameters") InterventionParams parameters,
            @JsonProperty("comments") List<Comment> comments) {
        super(id, name, status, createdBy, createDate, estateAddress, prestations, parameters, comments);
    }
}
