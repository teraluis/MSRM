package pdf.utils;

import estateclient.Annex;
import estateclient.Estate;
import estateclient.Locality;
import estateclient.Premises;
import core.models.Prestation;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SearchProperty {

    private final Set<Locality> localities;
    private final Set<Premises> premises;
    private final Set<Annex> annexes;

    private final Set<Prestation> prestations;

    // TODO à améliorer pour le tranformer en arbre

    public SearchProperty(Set<Estate> estates, Set<Prestation> prestations) {
        localities = new HashSet<>();
        premises = new HashSet<>();
        annexes = new HashSet<>();

        estates.forEach(e -> localities.addAll(e.localities));
        localities.forEach(l -> {
            premises.addAll(l.premises);
            annexes.addAll(l.annexes);
        });

        this.prestations = prestations;
    }

    public Object findEntityByBillLineId(String billLineId) {
        Prestation prestation = prestations
                .stream()
                .filter(p -> p.billLines.stream().anyMatch(b -> b.equals(billLineId)))
                .findFirst()
                .orElseThrow(() -> new NullPointerException(String.format("No prestation found for bill line: %s", billLineId)));

        String target = prestation.targetId.orElseThrow(() -> new NullPointerException("No target ID to find"));

        Optional<Locality> locality = localities.stream().filter(l -> target.equals(l.id)).findFirst();

        if (locality.isPresent()) {
            return locality.get();
        }

        Optional<Premises> premise = premises.stream().filter(p -> target.equals(p.id)).findFirst();
        if (premise.isPresent()) {
            return premise.get();
        }

        return annexes
                .stream()
                .filter(a -> target.equals(a.id))
                .findFirst()
                .orElseThrow(() -> new NullPointerException(String.format("Nothing found for %s", target)));
    }
}
