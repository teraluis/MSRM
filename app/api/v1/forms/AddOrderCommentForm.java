package api.v1.forms;

import play.data.validation.Constraints;

public class AddOrderCommentForm {

    @Constraints.Required
    protected String idOrder;
    @Constraints.Required
    protected AddUserForm user;
    @Constraints.Required
    protected String comment;
    @Constraints.Required
    protected String event;

    public AddOrderCommentForm() {
    }

    public String getIdOrder() {
        return idOrder;
    }

    public void setIdOrder(String idOrder) {
        this.idOrder = idOrder;
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

    public String getEvent() {
        return event;
    }

    public void setEvent(String eventType) {
        this.event = eventType;
    }
}
