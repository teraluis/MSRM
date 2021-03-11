package api.v1.forms;

import play.data.validation.Constraints;

public class AddAgencyForm {

    protected String uuid;
    @Constraints.Required
    protected String name;
    @Constraints.Required
    protected AddUserForm manager;
    @Constraints.Required
    protected String code;
    protected String referenceIban;
    protected String referenceBic;
    protected Long created;

    public AddAgencyForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) { this.uuid = uuid; }

    public String getName() { return name; }

    public void setName(final String name) { this.name = name; }

    public AddUserForm getManager() {
        return manager;
    }

    public void setManager(AddUserForm manager) {
        this.manager = manager;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getReferenceIban() {
        return referenceIban;
    }

    public void setReferenceIban(String referenceIban) {
        this.referenceIban = referenceIban;
    }

    public String getReferenceBic() {
        return referenceBic;
    }

    public void setReferenceBic(String referenceBic) {
        this.referenceBic = referenceBic;
    }
}
