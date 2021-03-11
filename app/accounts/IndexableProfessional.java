package accounts;

import core.search.Indexable;

import java.util.Optional;

public class IndexableProfessional implements Indexable {
    public final static String[] SEARCHABLE_FIELDS = {
            "id", "name", "category", "accountType", "status", "created", "people.name", "people.phone", "address",
            "commercial.name", "entity.name", "entity.siren"
    };
    protected String id;
    protected String name;
    protected String category;
    protected String accountType;
    protected Optional<String> status;
    protected String created;
    protected IndexableAccountPeople people;
    protected Optional<String> address;
    protected Optional<AccountUserIndexable> commercial;
    protected IndexableAccountEntity entity;

    public IndexableProfessional(final String id, final String name, final String category, final String accountType,
                                 final Optional<String> status, final String created, final IndexableAccountPeople people,
                                 final Optional<String> address, final Optional<AccountUserIndexable> commercial,
                                 final IndexableAccountEntity entity) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.accountType = accountType;
        this.status = status;
        this.created = created;
        this.people = people;
        this.address = address;
        this.commercial = commercial;
        this.entity = entity;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "professional";
    }

    @Override
    public String getTypeLabel() {
        return "professionnel";
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getAccountType() {
        return accountType;
    }

    public Optional<String> getStatus() {
        return status;
    }

    public String getCreated() {
        return created;
    }

    public IndexableAccountPeople getPeople() {
        return people;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public Optional<AccountUserIndexable> getCommercial() {
        return commercial;
    }

    public IndexableAccountEntity getEntity() {
        return entity;
    }

    public IndexableProfessional() {}

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setCategory(final String category) {
        this.category = category;
    }

    public void setAccountType(final String accountType) {
        this.accountType = accountType;
    }

    public void setStatus(final String status) {
        this.status = Optional.ofNullable(status);
    }

    public void setCreated(final String created) {
        this.created = created;
    }

    public void setPeople(final IndexableAccountPeople people) {
        this.people = people;
    }

    public void setAddress(final String address) {
        this.address = Optional.ofNullable(address);
    }

    public void setCommercial(final AccountUserIndexable commercial) {
        this.commercial = Optional.ofNullable(commercial);
    }

    public void setEntity(final IndexableAccountEntity entity) {
        this.entity = entity;
    }

    // Needed to parse JSON
    public void setTypeLabel(final String typeLabel) {
        // Nothing to do here
    }

    // Needed to parse JSON
    public void setType(final String type) {
        // Nothing to do here
    }
}
