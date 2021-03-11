package api.v1.forms;

import play.data.validation.Constraints;

public class ReportDestinationForm {

    public ReportDestinationForm(){
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public AddAddressForm getAddress() {
        return address;
    }

    public void setAddress(AddAddressForm address) {
        this.address = address;
    }

    public AddPeopleForm getPeople() {
        return people;
    }

    public void setPeople(AddPeopleForm people) {
        this.people = people;
    }

    public AddEstablishmentForm getEstablishment() {
        return establishment;
    }

    public void setEstablishment(AddEstablishmentForm establishment) {
        this.establishment = establishment;
    }

    protected String uuid;
    @Constraints.Required
    protected String order;
    protected String mail;
    protected String url;
    protected AddAddressForm address;
    protected AddPeopleForm people;
    protected AddEstablishmentForm establishment;
}
