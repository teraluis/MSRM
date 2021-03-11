package api.v1.forms;

import play.data.validation.Constraints;

public class AddEstablishmentForm {

    protected String uuid;
    @Constraints.Required
    protected String name;
    @Constraints.Required
    protected String corporateName;
    @Constraints.Required
    protected String siret;
    protected String sageCode;
    protected String description;
    protected String mail;
    protected String phone;
    @Constraints.Required
    protected AddActivityForm activity;
    @Constraints.Required
    protected String entity;
    protected Long created;
    @Constraints.Required
    protected String iban;
    @Constraints.Required
    protected String bic;
    protected String facturationAnalysis;
    @Constraints.Required
    protected AddAgencyForm agency;

    public AddEstablishmentForm() {
    }

    public AddAgencyForm getAgency() {
        return agency;
    }

    public void setAgency(AddAgencyForm agency) {
        this.agency = agency;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public void setCorporateName(final String corporateName) {
        this.corporateName = corporateName;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(final String siret) {
        this.siret = siret;
    }

    public String getSageCode() {
        return sageCode;
    }

    public void setSageCode(String sageCode) {
        this.sageCode = sageCode;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(final String description) {
        this.description = description;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(final String mail) {
        this.mail = mail;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public AddActivityForm getActivity() {
        return activity;
    }

    public void setActivity(final AddActivityForm activity) {
        this.activity = activity;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(final String entity) {
        this.entity = entity;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }

    public String getIban() {
        return this.iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getBic() {
        return this.bic;
    }

    public void setBic(String bic) {
        this.bic = bic;
    }

    public String getFacturationAnalysis() { return facturationAnalysis; }

    public void setFacturationAnalysis(String facturationAnalysis) { this.facturationAnalysis = facturationAnalysis; }

}
