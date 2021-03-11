package markets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Single;

import java.util.Optional;
import java.util.UUID;

public class Bpu implements Single<String> {
    public String uuid;
    public String file;
    public String market_id;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Bpu(
            @JsonProperty("uuid") final Optional<String> uuid,
            @JsonProperty("file")
            String file,
            @JsonProperty("marketId")
            String market_id
    ) {
        this.uuid = uuid.orElseGet(() -> "bpu-" + UUID.randomUUID());
        this.file = file;
        this.market_id = market_id;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
