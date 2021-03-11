package api.v1.models;

import core.EventType;
import users.User;

import java.util.Optional;

public class EstablishmentComment {
    public final String uuid;
    public final String idEstablishment;
    public final Optional<User> user;
    public final String comment;
    public final Long created;
    public final EventType event;

    public EstablishmentComment(String uuid, String idEstablishment, Optional<User> user, String comment, Long created, EventType event) {
        this.uuid = uuid;
        this.idEstablishment = idEstablishment;
        this.user = user;
        this.comment = comment;
        this.created = created;
        this.event = event;
    }

    public static EstablishmentComment serializeComment(establishments.EstablishmentComment comment, Optional<User> user) {
        return new EstablishmentComment(comment.uuid, comment.idEstablishment, user, comment.comment, comment.created.getTime(), comment.event);
    }
}
