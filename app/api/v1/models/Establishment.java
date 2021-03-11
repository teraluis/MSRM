package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.Address;
import core.models.People;

import java.util.Optional;

public class Establishment {

    public final String uuid;
    public final String name;
    public final String corporateName;
    public final String siret;
    public final String sageCode;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> mail;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> phone;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Activity> activity;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Address> address;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<People> contact;
    public final String entity;
    public final Long created;
    public final String iban;
    public final String bic;
    public final String facturationAnalysis;
    public final Agency agency;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Establishment(
            @JsonProperty("uuid") final String uuid,
            @JsonProperty("name") final String name,
            @JsonProperty("corporateName") final String corporateName,
            @JsonProperty("siret") final String siret,
            @JsonProperty("sageCode") final String sageCode,
            @JsonProperty("description") final String description,
            @JsonProperty("mail") final String mail,
            @JsonProperty("phone") final String phone,
            @JsonProperty("activity") final Activity activity,
            @JsonProperty("entity") final String entity,
            @JsonProperty("address") final Address address,
            @JsonProperty("contact") final People contact,
            @JsonProperty("created") final Long created,
            @JsonProperty("iban") final String iban,
            @JsonProperty("bic") final String bic,
            @JsonProperty("facturationAnalysis") final String facturationAnalysis,
            @JsonProperty("agency") final Agency agency
            )
        {
        this.uuid = uuid;
        this.name = name;
        this.corporateName = corporateName;
        this.siret = siret;
        this.sageCode = sageCode;
        this.description = Optional.ofNullable(description);
        this.mail = Optional.ofNullable(mail);
        this.phone = Optional.ofNullable(phone);
        this.activity = Optional.ofNullable(activity);
        this.entity = entity;
        this.address = Optional.ofNullable(address);
        this.contact = Optional.ofNullable(contact);
        this.created = created;
        this.iban = iban;
        this.bic = bic;
        this.facturationAnalysis = facturationAnalysis;
        this.agency = agency;
    }

    public static Establishment serialize(establishments.Establishment establishment, Optional<Address> address, Optional<People> contact, Optional<Activity> activity, Agency agency) {
        return new Establishment(
                establishment.uuid,
                establishment.name,
                establishment.corporateName,
                establishment.siret,
                establishment.sageCode,
                establishment.description.orElse(null),
                establishment.mail.orElse(null),
                establishment.phone.orElse(null),
                activity.orElse(null),
                establishment.entity,
                address.orElse(null),
                contact.orElse(null),
                establishment.created.getTime(),
                establishment.iban,
                establishment.bic,
                establishment.facturationAnalysis,
                agency
        );
    }
}
