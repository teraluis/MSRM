package api.v1.models;

import core.EventType;
import users.User;

import java.util.Optional;

public class AccountComment {
    public final String uuid;
    public final String idAccount;
    public final Optional<User> user;
    public final String comment;
    public final Long created;
    public final EventType event;

    public AccountComment(String uuid, String idAccount, Optional<User> user, String comment, Long created, EventType event) {
        this.uuid = uuid;
        this.idAccount = idAccount;
        this.user = user;
        this.comment = comment;
        this.created = created;
        this.event = event;
    }

    public static AccountComment serialize(accounts.AccountComment comment, Optional<User> user) {
        return new AccountComment(comment.uuid, comment.idAccount, user, comment.comment, comment.created.getTime(), comment.event);
    }
}
