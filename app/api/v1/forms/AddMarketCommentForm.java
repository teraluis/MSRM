package api.v1.forms;

import play.data.validation.Constraints;

public class AddMarketCommentForm {

    @Constraints.Required
    protected String idMarket;
    @Constraints.Required
    protected AddUserForm user;
    @Constraints.Required
    protected String comment;

    public AddMarketCommentForm() {
    }

    public String getIdMarket() {
        return idMarket;
    }

    public void setIdMarket(String idMarket) {
        this.idMarket = idMarket;
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
