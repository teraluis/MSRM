package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Optional;

public class Activity {

    public final String uuid;
    public final String name;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;
    public final Long created;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Activity(
            @JsonProperty("uuid") final String uuid,
            @JsonProperty("name") final String name,
            @JsonProperty("description") final String description,
            @JsonProperty("created") final Long created) {
        this.uuid = uuid;
        this.name = name;
        this.description = Optional.ofNullable(description);
        this.created = created;
    }

    public static Activity serialize(activities.Activity activity) {
        return new Activity(
                activity.uuid,
                activity.name,
                activity.description.orElse(null),
                activity.created.getTime()
        );
    }
}
