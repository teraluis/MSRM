package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import missionclient.interventions.MaterializedIntervention;

import java.util.List;
import java.util.Optional;

public class FactureWithDetails {

    public final Facture bill;
    public final Account account;
    public final String refNumber;
    public final Order order;
    public final List<MaterializedIntervention> interventions;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> address;

    public FactureWithDetails(Facture bill, Account account, String refNumber, Order order, List<MaterializedIntervention> interventions, Optional<String> address) {
        this.bill = bill;
        this.account = account;
        this.refNumber = refNumber;
        this.order = order;
        this.interventions = interventions;
        this.address = address;
    }

    public static FactureWithDetails serialize(Facture bill, Account account, Order order, List<MaterializedIntervention> interventions, Optional<String> address) {
        return new FactureWithDetails(bill, account, order.referenceNumber.orElse(""), order, interventions, address);
    }
}
