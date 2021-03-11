package api.v1.forms;

import play.data.validation.Constraints;

public class AddOrderForm {
    @Constraints.Required
    protected String name;
    @Constraints.Required
    protected String account;
    @Constraints.Required
    protected String status;
    protected Long created;
    protected String market;
    protected String estimate;
    protected String purchaserContact;
    protected String establishment;
    protected String commercial;
    protected Long received;

    public AddOrderForm() {
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(final String account) {
        this.account = account;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(final String market) {
        this.market = market;
    }

    public String getEstimate() {
        return estimate;
    }

    public void setEstimate(final String estimate) {
        this.estimate = estimate;
    }

    public String getPurchaserContact() {
        return purchaserContact;
    }

    public void setPurchaserContact(final String purchaserContact) {
        this.purchaserContact = purchaserContact;
    }

    public String getEstablishment() {
        return establishment;
    }

    public void setEstablishment(final String establishment) {
        this.establishment = establishment;
    }

    public String getCommercial() {
        return commercial;
    }

    public void setCommercial(String commercial) {
        this.commercial = commercial;
    }

    public Long getReceived() {
        return received;
    }

    public void setReceived(Long received) {
        this.received = received;
    }
}
