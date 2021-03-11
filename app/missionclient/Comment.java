package missionclient;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class Comment {
    public final String id;
    public final String idIntervention;
    public final String idUser;
    public final String comment;
    public final Date date;

    @JsonCreator
    public Comment(@JsonProperty("id") String id,
                   @JsonProperty("idIntervention") String idIntervention,
                   @JsonProperty("idUser") String idUser,
                   @JsonProperty("comment") String comment,
                   @JsonProperty("date") Date date) {
        this.id = id;
        this.idIntervention = idIntervention;
        this.idUser = idUser;
        this.comment = comment;
        this.date = date;
    }
}
