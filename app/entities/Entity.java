package entities;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Entity implements Single<String> {

    public final String uuid;
    public final String name;
    public final String corporateName;
    public final Optional<String> type;
    public final String siren;
    public final Optional<String> domain;
    public final Optional<String> logo;
    public final Optional<String> description;
    public final Optional<String> mainAddress;
    public final Date created;

    public Entity(final Optional<String> uuid, final String name, final String corporateName, final Optional<String> type, final String siren,
                  final Optional<String> domain, final Optional<String> logo, final Optional<String> description,
                  final Optional<String> mainAddress, final Date created) {
        this.uuid = uuid.orElseGet(() -> "entity-" + UUID.randomUUID());
        this.name = name;
        this.corporateName = corporateName;
        this.type = type;
        this.siren = siren;
        this.domain = domain;
        this.logo = logo;
        this.description = description;
        this.mainAddress = mainAddress;
        this.created = created;
    }

    @Override
    public String getId() {
        return uuid;
    }

}
