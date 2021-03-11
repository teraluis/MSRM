package estateWithAddress;

import addresses.Address;
import estateclient.Annex;
import estateclient.IdType;
import estateclient.Locality;
import estateclient.Premises;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class LocalityWithAddress {
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
    public final List<Address> addresses;
    public final List<Premises> premises;
    public final List<Annex> annexes;
    public final Boolean deleted;

    public LocalityWithAddress(
            String id,
            String name,
            Optional<String> floorQ,
            Optional<String> cadastralReference,
            Optional<Date> buildingPermitDate,
            Optional<Date> constructionDate,
            Optional<Boolean> condominium,
            Optional<String> inseeCoordinates,
            Date creationDate,
            Optional<IdType> heatingType,
            Optional<String> customHeatingType,
            List<Address> addresses,
            List<Premises> premises,
            List<Annex> annexes,
            Boolean deleted
    ) {
        this.id = id;
        this.name = name;
        this.floorQ = floorQ;
        this.cadastralReference = cadastralReference;
        this.buildingPermitDate = buildingPermitDate;
        this.constructionDate = constructionDate;
        this.condominium = condominium;
        this.inseeCoordinates = inseeCoordinates;
        this.creationDate = creationDate;
        this.heatingType = heatingType;
        this.customHeatingType = customHeatingType;
        this.addresses = addresses;
        this.premises = premises;
        this.annexes = annexes;
        this.deleted = deleted;
    }

    public static LocalityWithAddress buildWithAddress(Locality locality, List<Address> addresses) {
        return new LocalityWithAddress(
                locality.id,
                locality.name,
                locality.floorQ,
                locality.cadastralReference,
                locality.buildingPermitDate,
                locality.constructionDate,
                locality.condominium,
                locality.inseeCoordinates,
                locality.creationDate,
                locality.heatingType,
                locality.customHeatingType,
                addresses,
                locality.premises,
                locality.annexes,
                locality.deleted
        );
    }
}
