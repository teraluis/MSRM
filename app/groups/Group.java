package groups;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Group implements Single<String> {

    public final String uuid;
    public final String name;
    public final String type;
    public final Optional<String> category;
    public final Optional<String> iban;
    public final Optional<String> description;
    public final Date created;

    public Group(final Optional<String> uuid, final String name, final String type, final Optional<String> category,
                 final Optional<String> iban, final Optional<String> description, final Date created) {
        this.uuid = uuid.orElseGet(() -> "group-" + UUID.randomUUID());
        this.name = name;
        this.type = type;
        this.category = category;
        this.iban = iban;
        this.description = description;
        this.created = created;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
