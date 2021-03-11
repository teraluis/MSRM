package api.v1.forms;

import play.data.validation.Constraints;

public class AddActivityForm {

    protected String uuid;
    @Constraints.Required
    protected String name;
    protected String description;
    protected Long created;

    public AddActivityForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) { this.uuid = uuid; }

    public String getName() { return name; }

    public void setName(final String name) { this.name = name; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
