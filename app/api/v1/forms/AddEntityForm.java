package api.v1.forms;

import play.data.validation.Constraints;

public class AddEntityForm {

    protected String uuid;
    @Constraints.Required
    protected String name;
    @Constraints.Required
    protected String corporateName;
    protected String type;
    @Constraints.Required
    protected String siren;
    protected String domain;
    protected String logo;
    protected String description;
    protected AddAddressForm mainAddress;
    protected Long created;

    public AddEntityForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() { return name; }

    public void setName(final String name) { this.name = name; }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(final String corporateName) {
        this.corporateName = corporateName;
    }

    public String getType() {
        return type;
    }

    public void setType(final String type) {
        this.type = type;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(final String siren) {
        this.siren = siren;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(final String domain) {
        this.domain = domain;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(final String logo) {
        this.logo = logo;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public AddAddressForm getMainAddress() {
        return mainAddress;
    }

    public void setMainAddress(final AddAddressForm mainAddress) { this.mainAddress = mainAddress; }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
