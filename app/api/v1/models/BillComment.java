package api.v1.models;

import core.EventType;
import users.User;

import java.util.Optional;

public class BillComment {
    public final String uuid;
    public final String idBill;
    public final Optional<User> user;
    public final String comment;
    public final Long created;
    public final EventType event;

    public BillComment(String uuid, String idBill, Optional<User> user, String comment, Long created, EventType event) {
        this.uuid = uuid;
        this.idBill = idBill;
        this.user = user;
        this.comment = comment;
        this.created = created;
        this.event = event;
    }

    public static BillComment serialize(bills.BillComment comment, Optional<User> user) {
        return new BillComment(comment.uuid, comment.idBill, user, comment.comment, comment.created.getTime(), comment.event);
    }
}
