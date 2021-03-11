package missionclient.interventions;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Optional;

public class InterventionUtils {

    public static MaterializedIntervention buildIntervention(final MaterializedIntervention intervention) {
        switch (InterventionStatus.getFromLabel(intervention.getStatus())) {
            case CANCELED:
                return (CanceledIntervention) intervention;

            case DRAFT:
                return (DraftIntervention) intervention;

            case CREATED:
                return (CreatedIntervention) intervention;

            case SETTLED:
                return (SettledIntervention) intervention;

            case TO_SCHEDULE:
                return (ToScheduleIntervention) intervention;

            case SCHEDULED:
                return (ScheduledIntervention) intervention;

            case INCOMPLETE:
                return (IncompleteIntervention) intervention;

            case DONE:
                return (DoneIntervention) intervention;

            default:
                return intervention;
        }
    }

    public static Optional<MaterializedIntervention> buildIntervention(final JsonNode node) {
        return Optional.ofNullable(node.get("status")).map(jsonNode -> {
                    switch (InterventionStatus.getFromLabel(jsonNode.asText())) {
                        case CANCELED:
                            return new ObjectMapper().convertValue(node, CanceledIntervention.class);

                        case DRAFT:
                            return new ObjectMapper().convertValue(node, DraftIntervention.class);

                        case CREATED:
                            return new ObjectMapper().convertValue(node, CreatedIntervention.class);

                        case SETTLED:
                            return new ObjectMapper().convertValue(node, SettledIntervention.class);

                        case TO_SCHEDULE:
                            return new ObjectMapper().convertValue(node, ToScheduleIntervention.class);

                        case SCHEDULED:
                            return new ObjectMapper().convertValue(node, ScheduledIntervention.class);

                        case INCOMPLETE:
                            return new ObjectMapper().convertValue(node, IncompleteIntervention.class);

                        case DONE:
                            return new ObjectMapper().convertValue(node, DoneIntervention.class);

                        default:
                            throw new RuntimeException("could not create intervention object from json " + node.asText());
                    }
                }
        );
    }
}
