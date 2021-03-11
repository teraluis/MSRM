package missionclient.interventions;

public enum InterventionStatus {
    DRAFT(0, "DRAFT"),
    CREATED(1, "CREATED"),
    SETTLED(2, "SETTLED"),
    TO_SCHEDULE(3, "TO_SCHEDULE"),
    SCHEDULED(4, "SCHEDULED"),
    INCOMPLETE(5, "INCOMPLETE"),
    DONE(6, "DONE"),
    CANCELED(7, "CANCELED"),
    UNKNOWN(9, "UNKNOWN");

    public final int index;
    public final String label;

    InterventionStatus(int index, String label) {
        this.index = index;
        this.label = label;
    }

    public static InterventionStatus getFromLabel(String label) {
        switch (label) {
            case "CANCELED":
                return CANCELED;
            case "DRAFT":
                return DRAFT;
            case "CREATED":
                return CREATED;
            case "SETTLED":
                return SETTLED;
            case "TO_SCHEDULE":
                return TO_SCHEDULE;
            case "SCHEDULED":
                return SCHEDULED;
            case "INCOMPLETE":
                return INCOMPLETE;
            case "DONE":
                return DONE;
            default:
                return UNKNOWN;
        }
    }
}
