package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Date;
import java.util.Optional;

public class Group {

    public final String uuid;
    public final String name;
    public final String type;
    public final Optional<String> category;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> iban;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> description;
    public final Long created;

    public Group(
            final String uuid,
            final String name,
            final String type,
            final Optional<String> category,
            final Optional<String> iban,
            final Optional<String> description,
            final Long created
    ) {
        this.uuid = uuid;
        this.name = name;
        this.type = type;
        this.category = category;
        this.iban = iban;
        this.description = description;
        this.created = created;
    }

    public static Group serialize(groups.Group group) {
        return new Group(group.uuid, group.name, group.type, group.category, group.iban, group.description, group.created.getTime());
    }
}
