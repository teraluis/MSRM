package activities;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Activity implements Single<String> {

    public final String uuid;
    public final String name;
    public final Optional<String> description;
    public final Date created;

    public Activity(final Optional<String> uuid, final String name, final Optional<String> description, final Date created) {
        this.uuid = uuid.orElseGet(() -> "activity-" + UUID.randomUUID());
        this.name = name;
        this.description = description;
        this.created = created;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
