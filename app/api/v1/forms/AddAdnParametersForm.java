package api.v1.forms;

import play.data.validation.Constraints;

public class AddAdnParametersForm {

    @Constraints.Required
    protected String name;

    @Constraints.Required
    protected String adnName;

    @Constraints.Required
    protected Integer adnId;

    protected String address1;
    protected String address2;
    protected String zip;
    protected String city;
    protected String clienttype;

    public AddAdnParametersForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAdnName() {
        return adnName;
    }

    public void setAdnName(final String adnName) {
        this.adnName = adnName;
    }

    public Integer getAdnId() {
        return adnId;
    }

    public void setAdnId(final Integer adnId) {
        this.adnId = adnId;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(final String address1) {
        this.address1 = address1;
    }

    public String getAddress2() {
        return address2;
    }

    public void setAddress2(final String address2) {
        this.address2 = address2;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(final String zip) {
        this.zip = zip;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getClienttype() {
        return clienttype;
    }

    public void setClienttype(final String clienttype) {
        this.clienttype = clienttype;
    }

}
