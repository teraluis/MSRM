package estateWithAddress;

import estateclient.Attachment;
import estateclient.Estate;
import estateclient.IdType;

import java.util.List;
import java.util.Optional;

public class EstateWithAddress {
    public final String id;
    public final String adxReference;
    public final String name;
    public final Optional<String> estateReference;
    public final IdType estateType;
    public final Optional<String> customEstateType;
    public final String accountId; /*owner*/
    public final Integer state;
    public final List<LocalityWithAddress> localities;
    public final List<Attachment> attachments;
    public final Boolean deleted;

    public EstateWithAddress(
            String id,
            String adxReference,
            String name,
            Optional<String> estateReference,
            IdType estateType,
            Optional<String> customEstateType,
            String accountId,
            Integer state,
            List<LocalityWithAddress> localities,
            List<Attachment> attachments,
            Boolean deleted
    ) {
        this.id = id;
        this.adxReference = adxReference;
        this.name = name;
        this.estateReference = estateReference;
        this.estateType = estateType;
        this.customEstateType = customEstateType;
        this.accountId = accountId;
        this.state = state;
        this.localities = localities;
        this.attachments = attachments;
        this.deleted = deleted;
    }

    public static EstateWithAddress buildEstateWithAddress(Estate estate, List<LocalityWithAddress> localities) {
        return new EstateWithAddress(
                estate.id,
                estate.adxReference,
                estate.name,
                estate.estateReference,
                estate.estateType,
                estate.customEstateType,
                estate.accountId,
                estate.state,
                localities,
                estate.attachments,
                estate.deleted
        );
    }
}
