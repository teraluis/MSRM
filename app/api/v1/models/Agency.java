package api.v1.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import models.OfficeDao;
import users.User;

import java.util.List;

public class Agency {

    public final String uuid;
    public final String code;
    public final String name;
    public final User manager;
    public final Long created;
    public final String referenceIban;
    public final String referenceBic;
    public final List<OfficeDao> officies;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Agency(
            @JsonProperty("uuid")
            final String uuid,
            @JsonProperty("code")
            final String code,
            @JsonProperty("name")
            final String name,
            @JsonProperty("manager")
            final User manager,
            @JsonProperty("created")
            final Long created,
            @JsonProperty("referenceIban")
            final String referenceIban,
            @JsonProperty("referenceBic")
            final String referenceBic,
            @JsonProperty("officies")
            final List<OfficeDao> officies
    ) {
        this.uuid = uuid;
        this.code = code;
        this.name = name;
        this.manager = manager;
        this.created = created;
        this.referenceIban = referenceIban;
        this.referenceBic = referenceBic;
        this.officies = officies;
    }

    public static Agency serialize(agencies.Agency agency, User manager, List<OfficeDao> officies) {
        return new Agency(agency.uuid, agency.code, agency.name, manager, agency.created.getTime(), agency.referenceIban, agency.referenceBic, officies);
    }
}
