package orders;

import core.EventType;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class OrderComment implements Single<String> {
    public final String uuid;
    public final String idOrder;
    public final Optional<String> idUser;
    public final String comment;
    public final Date created;
    public final EventType eventType;

    public OrderComment(Optional<String> uuid, String idOrder, Optional<String> idUser, String comment, Date created, EventType eventType) {
        this.uuid = uuid.orElseGet(() -> "order_comment-" + UUID.randomUUID());
        this.idOrder = idOrder;
        this.idUser = idUser;
        this.comment = comment;
        this.created = created;
        this.eventType = eventType;
    }

    @Override
    public String getId() {
        return this.uuid;
    }
}
