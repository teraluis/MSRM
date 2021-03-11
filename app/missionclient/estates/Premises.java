package missionclient.estates;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Premises {
    public final String id;
    public final String number;
    public final Optional<String> floor;
    public final Optional<Double> area;
    public final Optional<Date> releaseDate;
    public final IdType premisesType;
    public final Optional<String> customPremisesType;
    public final Optional<String> idContact;
    public final Optional<IdType> heatingType;
    public final Optional<String> customHeatingType;
    public final Optional<String> premisesReference;
    public final Boolean deleted;

    public Premises(
            @JsonProperty("id") String id,
            @JsonProperty("number") String number,
            @JsonProperty("floor") String floor,
            @JsonProperty("area") Double area,
            @JsonProperty("releaseDate") Date releaseDate,
            @JsonProperty("premisesType") IdType premisesType,
            @JsonProperty("customPremisesType") String customPremisesType,
            @JsonProperty("idContact") String idContact,
            @JsonProperty("heatingType") IdType heatingType,
            @JsonProperty("customHeatingType") String customHeatingType,
            @JsonProperty("premisesReference") String premisesReference,
            @JsonProperty("deleted") Boolean deleted
    ) {
        this.id = id;
        this.number = number;
        this.floor = Optional.ofNullable(floor);
        this.area = Optional.ofNullable(area);
        this.releaseDate = Optional.ofNullable(releaseDate);
        this.premisesType = premisesType;
        this.customPremisesType = Optional.ofNullable(customPremisesType);
        this.idContact = Optional.ofNullable(idContact);
        this.heatingType = Optional.ofNullable(heatingType);
        this.customHeatingType = Optional.ofNullable(customHeatingType);
        this.premisesReference = Optional.ofNullable(premisesReference);
        this.deleted = deleted;
    }
}
