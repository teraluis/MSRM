package bills;

import core.EventType;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class BillComment implements Single<String> {
    public String getUuid() {
        return uuid;
    }

    public String getIdBill() {
        return idBill;
    }

    public Optional<String> getIdUser() {
        return idUser;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreated() {
        return created;
    }

    public final String uuid;
    public final String idBill;
    public final Optional<String> idUser;
    public final String comment;
    public final Date created;
    public final EventType event;

    public BillComment(Optional<String> uuid, String idBill, Optional<String> idUser, String comment, Date created, EventType event) {
        this.uuid = uuid.orElseGet(() -> "bill_comment-" + UUID.randomUUID());
        this.idBill = idBill;
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
