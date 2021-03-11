package agencies;

import core.Single;
import users.User;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Agency implements Single<String> {

    public final String uuid;
    public final String code;
    public final String name;
    public final String manager;
    public final Date created;
    public final String referenceIban;
    public final String referenceBic;

    public Agency(final Optional<String> uuid, final String code, final String name, final String manager, final Date created, final String referenceIban, final String referenceBic) {
        this.uuid = uuid.orElseGet(() -> "agency-" + UUID.randomUUID());
        this.code = code;
        this.name = name;
        this.manager = manager;
        this.created = created;
        this.referenceIban = referenceIban;
        this.referenceBic = referenceBic;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
