package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import missionclient.PrestationWithEstate;

import java.util.List;
import java.util.Optional;

public class Prestation {

    public final String uuid;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> status;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> mission;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> technicalAct;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> comment;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> resultId;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Boolean> unplanned;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> orderLine;
    public final List<String> billLines;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Estate> estate;

    public Prestation(final String uuid, final Optional<String> status, final Optional<String> mission, final Optional<String> technicalAct, final Optional<String> comment, final Optional<String> resultId, Optional<Boolean> unplanned, Optional<String> orderLine, List<String> billLines, final Optional<Estate> estate) {
        this.uuid = uuid;
        this.status = status;
        this.mission = mission;
        this.technicalAct = technicalAct;
        this.comment = comment;
        this.resultId = resultId;
        this.unplanned = unplanned;
        this.orderLine = orderLine;
        this.billLines = billLines;
        this.estate = estate;
    }

    public static Prestation serialize(PrestationWithEstate prestationWithEstate) {
        final Optional<Estate> finalEstate;
        if (prestationWithEstate.estate.isPresent()
                && !prestationWithEstate.estate.get().localities.isEmpty()
                && !prestationWithEstate.estate.get().localities.get(0).addresses.isEmpty()) {
            finalEstate = Optional.of(new Estate(prestationWithEstate.estate.get().id, prestationWithEstate.estate.get().localities.get(0).addresses.get(0)));
        } else {
            finalEstate = Optional.empty();
        }
        return new Prestation(prestationWithEstate.uuid, prestationWithEstate.status, prestationWithEstate.mission, prestationWithEstate.technicalAct, prestationWithEstate.comment, prestationWithEstate.resultId, prestationWithEstate.unplanned, prestationWithEstate.orderLine, prestationWithEstate.billLines, finalEstate);
    }
}
