package models;

import io.ebean.Model;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "attachments")
public class AttachmentDao extends Model {
    @Id
    @GeneratedValue(generator = "shortUid")
    @Column
    private String uuid;

    @Column
    private String vfk;

    @Column
    private String filename;

    @Column
    private String tenant;

    @Column
    private String attachmentType;

    @Column
    private String attachedUuid;

    @Column
    private Date created = new Date();

    @ManyToOne
    @JoinColumn(name = "id_user")
    private UserDao user;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getTenant() {
        return tenant;
    }

    public void setTenant(String tenant) {
        this.tenant = tenant;
    }

    public String getVfk() {
        return vfk;
    }

    public void setVfk(String vfk) {
        this.vfk = vfk;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public UserDao getUser() {
        return user;
    }

    public void setUser(UserDao user) {
        this.user = user;
    }

    public String getAttachmentType() {
        return attachmentType;
    }

    public void setAttachmentType(String attachmentType) {
        this.attachmentType = attachmentType;
    }

    public String getAttachedUuid() {
        return attachedUuid;
    }

    public void setAttachedUuid(String attachedUuid) {
        this.attachedUuid = attachedUuid;
    }
}

