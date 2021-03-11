package bills;


public enum BillStatus {

    PENDING("pending"),
    CONFIRMED("confirmed"),
    BILLED("billed"),
    CANCELLED("cancelled"),
    RECOVERY("recovery"),
    PAID("paid"),
    UNKNOWN("unknown");

    private final String id;

    BillStatus(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public static BillStatus fromId(final String id) {
        switch (id) {
            case "pending":
                return PENDING;
            case "confirmed":
                return CONFIRMED;
            case "billed":
                return BILLED;
            case "cancelled":
                return CANCELLED;
            case "recovery":
                return RECOVERY;
            case "paid":
                return PAID;
            default:
                return UNKNOWN;
        }
    }

}
