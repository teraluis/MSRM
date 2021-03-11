package api.v1.forms;

import play.data.validation.Constraints;

import java.util.List;

public class AddDelegateForm {

    @Constraints.Required
    protected String establishmentId;
    @Constraints.Required
    protected List<String> delegates;
    @Constraints.Required
    protected String type;

    public AddDelegateForm() {
    }

    public String getEstablishmentId() {
        return establishmentId;
    }

    public void setEstablishmentId(final String establishmentId) { this.establishmentId = establishmentId; }

    public List<String> getDelegates() { return delegates; }

    public void setDelegates(final List<String> delegates) { this.delegates = delegates; }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
