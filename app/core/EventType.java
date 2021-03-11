package core;

public enum EventType {
    STATUS ("STATUS"),
    MODIFICATION ("MODIFICATION"),
    MESSAGE ("MESSAGE");

    private String name = "";
    EventType(String name) {
        this.setName(name);
    }

    public String getName() {
        return name;
    }

    public String setName(String name) {
        switch (name.toUpperCase()) {
            case "STATUS":
                this.name = "STATUS";
                break;
            case "MODIFICATION":
                this.name = "MODIFICATION";
            case "MESSAGE":
                this.name = "MESSAGE";
                break;
            default: this.name = "MESSAGE";
        }
        return this.name;
    }
}
