package bills;

public class AccountForIndexableBill {
    private String name;
    private String id;

    public String getName() {
        return name;
    }

    public AccountForIndexableBill setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public AccountForIndexableBill setId(String id) {
        this.id = id;
        return this;
    }
}
