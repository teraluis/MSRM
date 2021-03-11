package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import bills.CreditNote;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public class Avoir {
    public final String uuid;
    public final String name;
    public final Date date;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Date> exportdate;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final List<FactureLigne> lignes;


    public Avoir(String uuid, String name, Date date, Optional<Date> exportdate, List<FactureLigne> lignes) {
        this.uuid = uuid;
        this.name = name;
        this.date = date;
        this.exportdate = exportdate;
        this.lignes = lignes;
    }

    public static Avoir serialize(bills.CreditNote avoir, List<FactureLigne> lignes) {
        return new Avoir(avoir.uuid, avoir.name, avoir.date, avoir.exportdate, lignes);
    }

}
