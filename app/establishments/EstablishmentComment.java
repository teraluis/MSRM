package establishments;

import core.EventType;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class EstablishmentComment implements Single<String> {

    public final String uuid;
    public final String idEstablishment;
    public final Optional<String> idUser;
    public final String comment;
    public final Date created;
    public final EventType event;
    public EstablishmentComment(Optional<String> uuid, String idEtablishment, Optional<String> idUser, String comment, Date created, EventType event) {
        this.uuid = uuid.orElseGet(() -> "establishment_comment-" + UUID.randomUUID());
        this.idEstablishment = idEtablishment;
        this.idUser = idUser;
        this.comment = comment;
        this.created = created;
        this.event = event;
    }

    @Override
    public String getId() {
        return this.uuid;
    }
}
