package bills;

import core.Single;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class Bill implements Single<String> {

    public final String uuid;
    public final String name;
    public final Boolean accompte;
    public final BillStatus status;
    public final Optional<String> recoverystatus;
    public final Date deadline;
    public final Optional<Date> exportdate;
    public final String order;


    public Bill(final Optional<String> uuid, final String name, final Boolean accompte, final BillStatus status, final Optional<String> recoverystatus, final String order, Date deadline, Optional<Date> exportdate) {
        this.uuid = uuid.orElseGet(() -> "bill-" + UUID.randomUUID());
        this.name = name;
        this.accompte = accompte;
        this.status = status;
        this.recoverystatus = recoverystatus;
        this.order = order;
        this.deadline = deadline;
        this.exportdate = exportdate;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
