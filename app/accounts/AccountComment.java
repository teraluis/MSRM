package accounts;

import core.EventType;
import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class AccountComment implements Single<String> {
    public final String uuid;
    public final String idAccount;
    public final Optional<String> idUser;
    public final String comment;
    public final Date created;
    public final EventType event;

    public AccountComment(Optional<String> uuid, String idAccount, Optional<String> idUser, String comment, Date created, EventType event) {
        this.uuid = uuid.orElseGet(() -> "account_comment-" + UUID.randomUUID());
        this.idAccount = idAccount;
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
