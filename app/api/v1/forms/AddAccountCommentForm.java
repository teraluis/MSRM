package api.v1.forms;

import play.data.validation.Constraints;

public class AddAccountCommentForm {
    @Constraints.Required
    protected String idAccount;
    @Constraints.Required
    protected AddUserForm user;
    @Constraints.Required
    protected String comment;

    public AddAccountCommentForm() {}

    public String getIdAccount() {
        return idAccount;
    }

    public void setIdAccount(String idAccount) {
        this.idAccount = idAccount;
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
