package establishments;

import accounts.Account;
import core.search.Indexable;

import java.util.Optional;

public class IndexableEstablishment implements Indexable {
    public final static String[] SEARCHABLE_FIELDS = {
            "id", "name", "corporateName", "siret", "created", "entity.name", "people.name", "people.phone",
            "description", "activity", "address", "phone", "mail"
    };
    protected String id;
    protected String name;
    protected String corporateName;
    protected String siret;
    protected String created;
    protected EstablishmentEntity entity;
    protected Optional<EstablishmentPeople> people;
    protected Optional<String> description;
    protected Optional<String> activity;
    protected Optional<String> address;
    protected Optional<String> phone;
    protected Optional<String> mail;
    protected EstablishmentAccount account;

    public IndexableEstablishment(final String id, final String name, final String corporateName, final String siret,
                                  final String created, final EstablishmentEntity entity, final Optional<EstablishmentPeople> people,
                                  final Optional<String> description, final Optional<String> activity, final Optional<String> address,
                                  final Optional<String> phone, final Optional<String> mail, final EstablishmentAccount account) {
        this.id = id;
        this.name = name;
        this.corporateName = corporateName;
        this.siret = siret;
        this.created = created;
        this.entity = entity;
        this.people = people;
        this.description = description;
        this.activity = activity;
        this.address = address;
        this.phone = phone;
        this.mail = mail;
        this.account = account;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "establishment";
    }

    @Override
    public String getTypeLabel() {
        return "établissement";
    }

    public String getName() {
        return name;
    }

    public String getCorporateName() {
        return corporateName;
    }

    public String getSiret() {
        return siret;
    }

    public String getCreated() {
        return created;
    }

    public EstablishmentEntity getEntity() {
        return entity;
    }

    public Optional<EstablishmentPeople> getPeople() {
        return people;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getActivity() {
        return activity;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public Optional<String> getPhone() {
        return phone;
    }

    public Optional<String> getMail() {
        return mail;
    }

    public IndexableEstablishment() {}

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCorporateName(final String corporateName) {
        this.corporateName = corporateName;
    }

    public void setSiret(final String siret) {
        this.siret = siret;
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public void setEntity(final IndexableEstablishment.EstablishmentEntity entity) {
        this.entity = entity;
    }

    public void setPeople(final IndexableEstablishment.EstablishmentPeople people) {
        this.people = Optional.ofNullable(people);
    }

    public void setDescription(final String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setActivity(final String activity) {
        this.activity = Optional.ofNullable(activity);
    }

    public void setAddress(final String address) {
        this.address = Optional.ofNullable(address);
    }

    public void setPhone(final String phone) {
        this.phone = Optional.ofNullable(phone);
    }

    public void setMail(final String mail) {
        this.mail = Optional.ofNullable(mail);
    }

    // Needed to parse JSON
    public void setTypeLabel(final String typeLabel) {
        // Nothing to do here
    }

    // Needed to parse JSON
    public void setType(final String type) {
        // Nothing to do here
    }

    public EstablishmentAccount getAccount() {
        return account;
    }

    public IndexableEstablishment setAccount(EstablishmentAccount account) {
        this.account = account;
        return this;
    }

    protected static class EstablishmentPeople {
        String id;
        String name;
        String phone;

        public EstablishmentPeople() {}

        public EstablishmentPeople(String id, String name, String phone) {
            this.id = id;
            this.name = name;
            this.phone = phone;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getPhone() {
            return phone;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }

        public void setPhone(final String phone) {
            this.phone = phone;
        }
    }

    protected static class EstablishmentEntity {
        String id;
        String name;

        public EstablishmentEntity() {}

        public EstablishmentEntity(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
