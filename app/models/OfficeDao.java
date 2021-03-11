package models;

import io.ebean.Model;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "office")
public class OfficeDao extends Model {
    @Id
    @GeneratedValue(generator = "shortUid")
    @Column
    private String uuid;

    @Column
    private String name;

    @Column(name = "id_agency")
    private String agency;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAgency() {
        return agency;
    }

    public void setAgency(String agency) {
        this.agency = agency;
    }
}

