package markets;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class MarketComment implements Single<String> {
    public final String uuid;

    public String getUuid() {
        return uuid;
    }

    public String getIdMarket() {
        return idMarket;
    }

    public Optional<String> getIdUser() {
        return this.idUser;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreated() {
        return created;
    }

    public final String idMarket;
    public final Optional<String> idUser;
    public final String comment;
    public final Date created;
    public MarketComment(Optional<String> uuid, String idMarket, Optional<String> idUser, String comment, Date created) {
        this.uuid = uuid.orElseGet(() -> "market_comment-" + UUID.randomUUID());
        this.idMarket = idMarket;
        this.idUser = idUser;
        this.comment = comment;
        this.created = created;
    }

    @Override
    public String getId() {
        return this.uuid;
    }
}
