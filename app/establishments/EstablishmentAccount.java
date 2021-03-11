package establishments;

public class EstablishmentAccount {
    private String id;
    private String status;
    private String category;

    public EstablishmentAccount() {
    }

    public EstablishmentAccount(String id, String status, String category) {
        this.id = id;
        this.status = status;
        this.category = category;
    }

    public String getId() {
        return id;
    }

    public EstablishmentAccount setId(String id) {
        this.id = id;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public EstablishmentAccount setStatus(String status) {
        this.status = status;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public EstablishmentAccount setCategory(String category) {
        this.category = category;
        return this;
    }
}
