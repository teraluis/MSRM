package api.v1.forms;

import play.data.validation.Constraints;

public class AddAddressForm {

    protected String uuid;
    @Constraints.Required
    protected String type;
    protected String address1;
    protected String address2;
    protected String postCode;
    protected String city;
    protected String gpsCoordinates;
    protected String inseeCoordinates;
    protected String dispatch;
    protected String staircase;
    protected String wayType;
    protected String country;
    protected Long created;

    public AddAddressForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getType() { return type; }

    public void setType(final String type) { this.type = type; }

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

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(final String postCode) {
        this.postCode = postCode;
    }

    public String getCity() {
        return city;
    }

    public void setCity(final String city) {
        this.city = city;
    }

    public String getGpsCoordinates() {
        return gpsCoordinates;
    }

    public void setGpsCoordinates(final String gpsCoordinates) {
        this.gpsCoordinates = gpsCoordinates;
    }

    public String getInseeCoordinates() {
        return inseeCoordinates;
    }

    public void setInseeCoordinates(final String inseeCoordinates) {
        this.inseeCoordinates = inseeCoordinates;
    }

    public String getDispatch() {
        return dispatch;
    }

    public void setDispatch(final String dispatch) {
        this.dispatch = dispatch;
    }

    public String getStaircase() {
        return staircase;
    }

    public void setStaircase(final String staircase) {
        this.staircase = staircase;
    }

    public String getWayType() {
        return wayType;
    }

    public void setWayType(final String wayType) { this.wayType = wayType; }

    public String getCountry() {
        return country;
    }

    public void setCountry(final String country) { this.country = country; }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
