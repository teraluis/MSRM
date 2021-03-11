package api.v1.models;

import core.EventType;
import users.User;

import java.util.Optional;

public class OrderComment {
    public final String uuid;
    public final String idOrder;
    public final Optional<User> user;
    public final String comment;
    public final Long created;
    public final EventType event;

    public OrderComment(String uuid, String idOrder, Optional<User> user, String comment, Long created, EventType event) {
        this.uuid = uuid;
        this.idOrder = idOrder;
        this.user = user;
        this.comment = comment;
        this.created = created;
        this.event = event;
    }

    public static OrderComment serialize(orders.OrderComment comment, Optional<User> user) {
        return new OrderComment(comment.uuid, comment.idOrder, user , comment.comment, comment.created.getTime(), comment.eventType);
    }
}
