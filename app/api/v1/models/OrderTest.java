package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import estateclient.Estate;
import missionclient.interventions.MaterializedIntervention;

import java.util.List;
import java.util.Optional;

public class OrderTest {

    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final api.v1.models.Order order;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final List<Facture> bills;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final List<PrestationWithResult> prestations;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Estate> estate;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<MaterializedIntervention> intervention;

    public OrderTest(final api.v1.models.Order order, final List<Facture> bills, final List<PrestationWithResult> prestations, final Optional<Estate> estate, final Optional<MaterializedIntervention> intervention) {
        this.order = order;
        this.bills = bills;
        this.prestations = prestations;
        this.estate = estate;
        this.intervention = intervention;
    }
}
