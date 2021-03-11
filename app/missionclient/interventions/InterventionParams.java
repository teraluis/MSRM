package missionclient.interventions;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.PeopleWithRole;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InterventionParams {
    protected final String contactName;
    protected final String contactType;
    protected final String accessConditions;
    protected final String additionalInformations;
    protected final String accessDetails;
    protected final String workDescription;
    protected final Date interventionDate;
    protected final Date closureDate;
    protected final String reportId;
    protected final List<PeopleWithRole> contacts;

    @JsonCreator
    public InterventionParams(
            @JsonProperty("contactName") String contactName,
            @JsonProperty("contactType") String contactType,
            @JsonProperty("accessConditions") String accessConditions,
            @JsonProperty("additionalInformations") String additionalInformations,
            @JsonProperty("accessDetails") String accessDetails,
            @JsonProperty("workDescription") String workDescription,
            @JsonProperty("interventionDate") Date interventionDate,
            @JsonProperty("closureDate") Date closureDate,
            @JsonProperty("reportId") String reportId,
            @JsonProperty("contacts") List<PeopleWithRole> contacts
    ) {
        this.contactName = contactName;
        this.contactType = contactType;
        this.accessConditions = accessConditions;
        this.additionalInformations = additionalInformations;
        this.accessDetails = accessDetails;
        this.workDescription = workDescription;
        this.interventionDate = interventionDate;
        this.closureDate = closureDate;
        this.reportId = reportId;
        this.contacts = contacts;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactType() {
        return contactType;
    }

    public String getAccessConditions() {
        return accessConditions;
    }

    public String getAdditionalInformations() {
        return additionalInformations;
    }

    public String getAccessDetails() {
        return accessDetails;
    }

    public String getWorkDescription() {
        return workDescription;
    }

    public Date getInterventionDate() {
        return interventionDate;
    }

    public Date getClosureDate() {
        return closureDate;
    }

    public String getReprotId() {
        return reportId;
    }

    public List<PeopleWithRole> getContacts() {
        return contacts;
    }
}
