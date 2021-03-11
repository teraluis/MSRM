package api.v1.forms;

import play.data.validation.Constraints;

public class AddBillCommentForm {

    @Constraints.Required
    protected String idBill;
    @Constraints.Required
    protected AddUserForm user;
    @Constraints.Required
    protected String comment;

    public AddBillCommentForm() {
    }

    public String getIdBill() {
        return idBill;
    }

    public void setIdBill(String idBill) {
        this.idBill = idBill;
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
