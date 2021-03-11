package api.v1.forms;

import play.data.validation.Constraints;

public class AddUserForm {

    protected String login;
    protected String registration_number;
    @Constraints.Required
    protected String first_name;
    @Constraints.Required
    protected String last_name;
    protected String office;
    protected String phone;
    @Constraints.Required
    protected String description;

    public AddUserForm() {
    }

    public String getLogin() { return login; }

    public void setLogin(final String login) { this.login = login; }

    public String getRegistration_number() { return registration_number; }

    public void setRegistration_number(final String registration_number) { this.registration_number = registration_number; }

    public String getFirst_name() { return first_name; }

    public void setFirst_name(final String first_name) { this.first_name = first_name; }

    public String getLast_name() { return last_name; }

    public void setLast_name(final String last_name) { this.last_name = last_name; }

    public String getOffice() { return office; }

    public void setOffice(final String office) { this.office = office; }

    public String getPhone() { return phone; }

    public void setPhone(final String phone) { this.phone = phone; }

    public String getDescription() { return description; }

    public void setDescription(final String description) { this.description = description; }
}
