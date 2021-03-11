package api.v1.forms;

import play.data.validation.Constraints;

import java.util.List;

public class AddAccountForm {

    protected String uuid;
    protected String sageCode;
    @Constraints.Required
    protected String type;
    @Constraints.Required
    protected String reference;
    @Constraints.Required
    protected String category;
    @Constraints.Required
    protected AddUserForm commercial;
    @Constraints.Required
    protected AddPeopleForm contact;
    protected String importance;
    @Constraints.Required
    protected String state;
    protected AddEntityForm entity;
    protected Integer maxPaymentTime;
    protected List<String> groups;
    protected Long created;

    public AddAccountForm() {
    }

    public String getUuid() { return uuid; }

    public void setUuid(final String uuid) { this.uuid = uuid; }

    public String getSageCode() { return sageCode; }

    public void setSageCode(final String sageCode) { this.sageCode = sageCode; }

    public String getType() { return type; }

    public void setType(final String type) { this.type = type; }

    public String getReference() {
        return reference;
    }

    public void setReference(final String reference) {
        this.reference = reference;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public AddUserForm getCommercial() {
        return commercial;
    }

    public void setCommercial(final AddUserForm commercial) {
        this.commercial = commercial;
    }

    public AddPeopleForm getContact() {
        return contact;
    }

    public void setContact(final AddPeopleForm contact) {
        this.contact = contact;
    }

    public String getImportance() {
        return importance;
    }

    public void setImportance(final String importance) {
        this.importance = importance;
    }

    public String getState() {
        return state;
    }

    public void setState(final String state) {
        this.state = state;
    }

    public AddEntityForm getEntity() {
        return entity;
    }

    public void setEntity(final AddEntityForm entity) {
        this.entity = entity;
    }

    public Integer getMaxPaymentTime() {
        return maxPaymentTime;
    }

    public void setMaxPaymentTime(final Integer maxPaymentTime) {
        this.maxPaymentTime = maxPaymentTime;
    }

    public List<String> getGroups() { return groups; }

    public void setGroups(final List<String> groups) { this.groups = groups; }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) { this.created = created; }
}
