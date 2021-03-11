package orders;

public enum OrderStatus {

    RECEIVED("received"),
    FILLED("filled"),
    PRODUCTION("production"),
    BILLABLE("billable"),
    HONORED("honored"),
    CLOSED("closed"),
    CANCELED("canceled"),
    UNKNOWN("unknown");

    private final String id;

    OrderStatus(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static OrderStatus fromId(final String id) {
        switch (id) {
            case "received":
                return RECEIVED;
            case "filled":
                return FILLED;
            case "production":
                return PRODUCTION;
            case "billable":
                return BILLABLE;
            case "honored":
                return HONORED;
            case "closed":
                return CLOSED;
            case "canceled":
                return CANCELED;
            default:
                return UNKNOWN;
        }
    }
}
