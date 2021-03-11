package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DraftIntervention implements MaterializedIntervention {
    public final String id;
    public final String name;
    public final String status;
    public final String createdBy;
    public final Date createDate;
    public final Optional<String> estateAddress;

    @JsonCreator
    public DraftIntervention(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("status") String status,
            @JsonProperty("createdBy") String createdBy,
            @JsonProperty("createDate") Date createDate,
            @JsonProperty("estateAddress") String estateAddress) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.createdBy = createdBy;
        this.createDate = createDate;
        this.estateAddress = Optional.ofNullable(estateAddress);
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public Date getCreateDate() {
        return createDate;
    }
}
