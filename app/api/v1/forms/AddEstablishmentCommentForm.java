package api.v1.forms;

import play.data.validation.Constraints;

public class AddEstablishmentCommentForm {

    @Constraints.Required
    protected String idEstablishment;
    @Constraints.Required
    protected AddUserForm user;
    @Constraints.Required
    protected String comment;

    public AddEstablishmentCommentForm() { }

    public String getIdEstablishment() {
        return idEstablishment;
    }

    public void setIdEstablishment(String idEstablishment) {
        this.idEstablishment = idEstablishment;
    }

    public AddUserForm getUser() {
        return user;
    }

    public void setUser(AddUserForm user) {
        this.user = user;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
