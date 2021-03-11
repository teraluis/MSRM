package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class OrderRecap {

    public final String status;
    public final String clientName;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> marketName;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> referenceNumber;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<String> referenceFile;
    public final List<String> targets;
    public final Integer estateWithoutPrestations;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Integer> interventionCreated;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<Integer> billsCreated;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final Optional<BigDecimal> billedTotal;


    public OrderRecap(String status, String clientName, Optional<String> marketName, Optional<String> referenceNumber, Optional<String> referenceFile, List<String> targets, Integer estateWithoutPrestations, Optional<Integer> interventionCreated, Optional<Integer> billsCreated, Optional<BigDecimal> billedTotal) {
        this.status = status;
        this.clientName = clientName;
        this.marketName = marketName;
        this.referenceNumber = referenceNumber;
        this.referenceFile = referenceFile;
        this.targets = targets;
        this.estateWithoutPrestations = estateWithoutPrestations;
        this.interventionCreated = interventionCreated;
        this.billsCreated = billsCreated;
        this.billedTotal = billedTotal;
    }
}
