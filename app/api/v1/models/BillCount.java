package api.v1.models;

public class BillCount {
    public final Integer total;
    public final Integer pending;
    public final Integer confirmed;
    public final Integer billed;
    public final Integer cancelled;
    public final Integer paid;

    public BillCount(Integer total, Integer pending, Integer confirmed, Integer billed, Integer cancelled, Integer paid) {
        this.total = total;
        this.pending = pending;
        this.confirmed = confirmed;
        this.billed = billed;
        this.cancelled = cancelled;
        this.paid = paid;
    }
}
