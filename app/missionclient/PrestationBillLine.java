package missionclient;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PrestationBillLine {

    public final String prestation;
    public final String billLine;

    public PrestationBillLine(
            @JsonProperty("prestation") String prestation,
            @JsonProperty("billLine") String billLine) {
        this.prestation = prestation;
        this.billLine = billLine;
    }
}