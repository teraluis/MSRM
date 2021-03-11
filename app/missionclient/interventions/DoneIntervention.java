package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.Comment;
import core.models.Prestation;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DoneIntervention extends IncompleteIntervention {
    @JsonCreator
    public DoneIntervention(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createDate") Date createDate,
            @JsonProperty("estateAddress") String estateAddress,
            @JsonProperty("prestations") List<Prestation> prestations,
            @JsonProperty("parameters") InterventionParams parameters,
            @JsonProperty("planning") InterventionPlanning planning,
            @JsonProperty("comments") List<Comment> comments,
            @JsonProperty("bills") List<String> bills) {
        super(id, name, status, createdBy, createDate, estateAddress, prestations, parameters, planning, comments, bills);
    }
}
