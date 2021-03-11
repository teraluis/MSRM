package accounts;

import java.util.Optional;

public class IndexableAccount {
    private String id;
    private String name;
    private String category;
    private String accountType;
    private Optional<String> status;
    private String created;
    private IndexableAccountPeople people;
    private Optional<String> address;
    private Optional<AccountUserIndexable> commercial;
    private IndexableAccountEntity entity;

    public String getId() {
        return id;
    }

    public IndexableAccount setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public IndexableAccount setName(String name) {
        this.name = name;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public IndexableAccount setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getAccountType() {
        return accountType;
    }

    public IndexableAccount setAccountType(String accountType) {
        this.accountType = accountType;
        return this;
    }

    public Optional<String> getStatus() {
        return status;
    }

    public IndexableAccount setStatus(Optional<String> status) {
        this.status = status;
        return this;
    }

    public String getCreated() {
        return created;
    }

    public IndexableAccount setCreated(String created) {
        this.created = created;
        return this;
    }

    public IndexableAccountPeople getPeople() {
        return people;
    }

    public IndexableAccount setPeople(IndexableAccountPeople people) {
        this.people = people;
        return this;
    }

    public Optional<String> getAddress() {
        return address;
    }

    public IndexableAccount setAddress(Optional<String> address) {
        this.address = address;
        return this;
    }

    public Optional<AccountUserIndexable> getCommercial() {
        return commercial;
    }

    public IndexableAccount setCommercial(Optional<AccountUserIndexable> commercial) {
        this.commercial = commercial;
        return this;
    }

    public IndexableAccountEntity getEntity() {
        return entity;
    }

    public IndexableAccount setEntity(IndexableAccountEntity entity) {
        this.entity = entity;
        return this;
    }
}
