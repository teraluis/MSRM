package api.v1.models;

import users.User;

import java.util.Optional;

public class MarketComment {
    public final String uuid;
    public final String idMarket;
    public final Optional<User> user;
    public final String comment;
    public final Long created;

    public MarketComment(String uuid, String idMarket, Optional<User> user, String comment, Long created) {
        this.uuid = uuid;
        this.idMarket = idMarket;
        this.user = user;
        this.comment = comment;
        this.created = created;
    }

    public static MarketComment serialize(markets.MarketComment comment, Optional<User> user) {
        return new MarketComment(comment.uuid, comment.idMarket, user, comment.comment, comment.created.getTime());
    }
}
