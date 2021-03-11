package api.v1.forms;

import play.data.validation.Constraints;

public class AddPeopleForm {

    protected String uuid;
    @Constraints.Required
    protected String title;
    @Constraints.Required
    protected String lastname;
    @Constraints.Required
    protected String firstname;
    protected String workMail;
    protected String workPhone;
    protected String email;
    protected String mobilePhone;
    protected String jobDescription;

    public AddPeopleForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(final String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(final String firstname) {
        this.firstname = firstname;
    }

    public String getWorkMail() {
        return workMail;
    }

    public void setWorkMail(final String workMail) {
        this.workMail = workMail;
    }

    public String getWorkPhone() {
        return workPhone;
    }

    public void setWorkPhone(final String workPhone) {
        this.workPhone = workPhone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(final String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(final String jobDescription) {
        this.jobDescription = jobDescription;
    }
}
