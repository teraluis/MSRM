package api.v1.models;

public class OrderCount {

    public final Integer total;
    public final Integer received;
    public final Integer filled;
    public final Integer production;
    public final Integer billable;
    public final Integer honored;
    public final Integer closed;
    public final Integer deadlineOutdated;
    public final Integer deadlineClose;
    public final Integer deadlineOk;

    public OrderCount(Integer total, Integer received, Integer filled, Integer production, Integer billable, Integer honored, Integer closed, Integer deadlineOutdated, Integer deadlineClose, Integer deadlineOk) {
        this.total = total;
        this.received = received;
        this.filled = filled;
        this.production = production;
        this.billable = billable;
        this.honored = honored;
        this.closed = closed;
        this.deadlineOutdated = deadlineOutdated;
        this.deadlineClose = deadlineClose;
        this.deadlineOk = deadlineOk;
    }

}
