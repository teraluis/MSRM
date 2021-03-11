package estateclient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Locality {
    public final String id;
    public final String name;
    public final Optional<String> floorQ; /* floor quantity */
    public final Optional<String> cadastralReference;
    public final Optional<Date> buildingPermitDate;
    public final Optional<Date> constructionDate;
    public final Optional<Boolean> condominium;
    public final Optional<String> inseeCoordinates;
    public final Date creationDate;
    public final Optional<IdType> heatingType;
    public final Optional<String> customHeatingType;
    public final List<String> addresses;
    public final List<Premises> premises;
    public final List<Annex> annexes;
    public final Boolean deleted;

    public Locality(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("floorQ") String floorQ,
            @JsonProperty("cadastralReference") String cadastralReference,
            @JsonProperty("buildingPermitDate") Date buildingPermitDate,
            @JsonProperty("constructionDate") Date constructionDate,
            @JsonProperty("condominium") Boolean condominium,
            @JsonProperty("inseeCoordinates") String inseeCoordinates,
            @JsonProperty("creationDate") Date creationDate,
            @JsonProperty("heatingType") IdType heatingType,
            @JsonProperty("customHeatingType") String customHeatingType,
            @JsonProperty("addresses") List<String> addresses,
            @JsonProperty("premises") List<Premises> premises,
            @JsonProperty("annexes") List<Annex> annexes,
            @JsonProperty("deleted") Boolean deleted
    ) {
        this.id = id;
        this.name = name;
        this.floorQ = Optional.ofNullable(floorQ);
        this.cadastralReference = Optional.ofNullable(cadastralReference);
        this.buildingPermitDate = Optional.ofNullable(buildingPermitDate);
        this.constructionDate = Optional.ofNullable(constructionDate);
        this.condominium = Optional.ofNullable(condominium);
        this.inseeCoordinates = Optional.ofNullable(inseeCoordinates);
        this.creationDate = creationDate;
        this.heatingType = Optional.ofNullable(heatingType);
        this.customHeatingType = Optional.ofNullable(customHeatingType);
        this.addresses = addresses;
        this.premises = premises;
        this.annexes = annexes;
        this.deleted = deleted;
    }
}
