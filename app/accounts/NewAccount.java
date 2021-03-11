package accounts;

public class NewAccount {
    private String uuid;
    private String name;
    private String siren;
    private String address;
    private String category;
    private String type;
    private String state;
    private String commercial;

    public String getUuid() {
        return uuid;
    }

    public NewAccount setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NewAccount setName(String name) {
        this.name = name;
        return this;
    }

    public String getSiren() {
        return siren;
    }

    public NewAccount setSiren(String siren) {
        this.siren = siren;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public NewAccount setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getCategory() {
        return category;
    }

    public NewAccount setCategory(String category) {
        this.category = category;
        return this;
    }

    public String getType() {
        return type;
    }

    public NewAccount setType(String type) {
        this.type = type;
        return this;
    }

    public String getState() {
        return state;
    }

    public NewAccount setState(String state) {
        this.state = state;
        return this;
    }

    public String getCommercial() {
        return commercial;
    }

    public NewAccount setCommercial(String commercial) {
        this.commercial = commercial;
        return this;
    }
}
