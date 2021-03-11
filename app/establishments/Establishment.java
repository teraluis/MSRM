package establishments;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Establishment implements Single<String> {

    public final String uuid;
    public final String name;
    public final String corporateName;
    public final String siret;
    public final String sageCode;
    public final Optional<String> description;
    public final Optional<String> mail;
    public final Optional<String> phone;
    public final Optional<String> activity;
    public final String entity;
    public final Date created;
    public final Boolean clientExported;
    public final Boolean clientModified;
    public final Boolean validatorExported;
    public final Boolean validatorModified;
    public final String iban;
    public final String bic;
    public final String facturationAnalysis;
    public final String agency;

    public Establishment(final Optional<String> uuid, final String name, final String corporateName, final String siret, final String sageCode,
                         final Optional<String> description, final Optional<String> mail, final Optional<String> phone,
                         final Optional<String> activity, final String entity, final Date created, final Boolean clientExported,
                         final Boolean clientModified, final Boolean validatorExported, final Boolean validatorModified,
                         final String iban, final String bic, final String facturationAnalysis, String agency) {
        this.uuid = uuid.orElseGet(() -> "establishment-" + UUID.randomUUID());
        this.name = name;
        this.corporateName = corporateName;
        this.siret = siret;
        this.sageCode = sageCode;
        this.description = description;
        this.mail = mail;
        this.phone = phone;
        this.activity = activity;
        this.entity = entity;
        this.created = created;
        this.clientExported = clientExported;
        this.clientModified = clientModified;
        this.validatorExported = validatorExported;
        this.validatorModified = validatorModified;
        this.iban = iban;
        this.bic = bic;
        this.facturationAnalysis = facturationAnalysis;
        this.agency = agency;
    }

    @Override
    public String getId() {
        return this.uuid;
    }
}

