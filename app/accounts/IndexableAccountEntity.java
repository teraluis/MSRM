package accounts;

public class IndexableAccountEntity {
    String id;
    String name;
    String siren;

    public IndexableAccountEntity() {}

    public IndexableAccountEntity(String id, String name, String siren) {
        this.id = id;
        this.name = name;
        this.siren = siren;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSiren() {
        return siren;
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setSiren(final String siren) {
        this.siren = siren;
    }
}
