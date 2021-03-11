package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import missionclient.Comment;
import core.models.Prestation;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class CreatedIntervention extends DraftIntervention {
    protected final List<Prestation> prestations;
    protected final List<Comment> comments;

    @JsonCreator
    public CreatedIntervention(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createDate") Date createDate,
            @JsonProperty("estateAddress") String estateAddress,
            @JsonProperty("prestations") List<Prestation> prestations,
            @JsonProperty("comments") List<Comment> comments) {
        super(id, name, status, createdBy, createDate, estateAddress);
        this.prestations = prestations;
        this.comments = comments;
    }

    public List<Prestation> getPrestations() {
        return prestations;
    }

    public List<Comment> getComments() {
        return comments;
    }
}
