package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import bills.Bill;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class Facture {

    public final String uuid;
    public final String name;
    public final Boolean accompte;
    public final Date deadline;
    public final String status;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> recoverystatus;
    public final List<FactureLigne> lignes;
    public final List<Paiement> paiements;
    public final List<Avoir> creditnotes;
    public final String order;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> exportDate;

    public Facture(final String uuid, final String name, final Boolean accompte, Date deadline, final String status, final Optional<String> recoverystatus, final String order, final List<FactureLigne> lignes, List<Paiement> paiements, List<Avoir> creditnotes, Optional<Date> exportDate) {
        this.uuid = uuid;
        this.name = name;
        this.accompte = accompte;
        this.deadline = deadline;
        this.status = status;
        this.recoverystatus = recoverystatus;
        this.lignes = lignes;
        this.order = order;
        this.paiements = paiements;
        this.creditnotes = creditnotes;
        this.exportDate = exportDate;
    }

    public static Facture serialize(bills.Bill facture, List<FactureLigne> factureLignes, List<Paiement> paiements, List<Avoir> creditnotes) {
        return new Facture(facture.uuid, facture.name, facture.accompte, facture.deadline, facture.status.getId(), facture.recoverystatus, facture.order, factureLignes, paiements, creditnotes, facture.exportdate);
    }


}
