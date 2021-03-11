package markets;

import api.v1.models.Agency;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.Single;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Market implements Single<String> {

    public final String uuid;
    public final String name;
    public final String marketNumber;
    public final Optional<String> status;
    public final String customerRequirement;
    public final String tenant;
    public final Optional<LocalDate> receiveDate;
    public final Optional<LocalDate> responseDate;
    public final Optional<LocalDate> returnDate;
    public final Optional<LocalDate> startDate;
    public final Optional<Integer> duration;
    public final Optional<String> publicationNumber;
    public final Optional<String> origin;
    public final Optional<String> estimateVolume;
    public final Optional<String> missionOrderType;
    public final Optional<String> deadlineModality;
    public final Optional<String> dunningModality;
    public final Optional<String> interventionCondition;
    public final Optional<String> specificReportNaming;
    public final Optional<String> specificReportDisplay;
    public final Optional<String> specificBilling;
    public final Optional<String> missionOrderBillingLink;
    public final Optional<String> billingFrequency;
    public final Optional<String> warningPoint;
    public final Optional<String> description;
    public final List<Bpu> bpu;
    public final List<MarketEstablishment> marketEstablishments;
    public final List<MarketPeople> marketPeoples;
    public final List<MarketUser> marketUsers;
    public final Agency agency;
    public final String facturationAnalysis;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public Market(
            @JsonProperty("uuid") final Optional<String> uuid,
            @JsonProperty("name") final String name,
            @JsonProperty("marketNumber") String marketNumber,
            @JsonProperty("status") final Optional<String> status,
            @JsonProperty("customerRequirement") final String customerRequirement,
            @JsonProperty("tenant") final String tenant,
            @JsonProperty("receiveDate") final Optional<LocalDate> receiveDate,
            @JsonProperty("responseDate") final Optional<LocalDate> responseDate,
            @JsonProperty("returnDate") final Optional<LocalDate> returnDate,
            @JsonProperty("startDate") final Optional<LocalDate> startDate,
            @JsonProperty("duration") final Optional<Integer> duration,
            @JsonProperty("publicationNumber") final Optional<String> publicationNumber,
            @JsonProperty("origin") final Optional<String> origin,
            @JsonProperty("estimateVolume") final Optional<String> estimateVolume,
            @JsonProperty("missionOrderType") final Optional<String> missionOrderType,
            @JsonProperty("deadlineModality") final Optional<String> deadlineModality,
            @JsonProperty("dunningModality") final Optional<String> dunningModality,
            @JsonProperty("interventionCondition") final Optional<String> interventionCondition,
            @JsonProperty("specificReportNaming") final Optional<String> specificReportNaming,
            @JsonProperty("specificReportDisplay") final Optional<String> specificReportDisplay,
            @JsonProperty("specificBilling") final Optional<String> specificBilling,
            @JsonProperty("missionOrderBillingLink") final Optional<String> missionOrderBillingLink,
            @JsonProperty("billingFrequency") final Optional<String> billingFrequency,
            @JsonProperty("warningPoint") final Optional<String> warningPoint,
            @JsonProperty("description") final Optional<String> description,
            @JsonProperty("bpuList") final List<Bpu> bpu,
            @JsonProperty("marketAccounts") final List<MarketEstablishment> marketEstablishments,
            @JsonProperty("marketPeoples") final List<MarketPeople> marketPeoples,
            @JsonProperty("marketUsers") final List<MarketUser> marketUsers,
            @JsonProperty("agency") final Agency agency,
            @JsonProperty("facturationAnalysis") final String facturationAnalysis
    ) {
        this.uuid = uuid.orElseGet(() -> "market-" + UUID.randomUUID());
        this.name = name;
        this.marketNumber = marketNumber;
        this.status = status;
        this.customerRequirement = customerRequirement;
        this.tenant = tenant;
        this.receiveDate = receiveDate;
        this.responseDate = responseDate;
        this.returnDate = returnDate;
        this.startDate = startDate;
        this.duration = duration;
        this.publicationNumber = publicationNumber;
        this.origin = origin;
        this.estimateVolume = estimateVolume;
        this.missionOrderType = missionOrderType;
        this.deadlineModality = deadlineModality;
        this.dunningModality = dunningModality;
        this.interventionCondition = interventionCondition;
        this.specificReportNaming = specificReportNaming;
        this.specificReportDisplay = specificReportDisplay;
        this.specificBilling = specificBilling;
        this.missionOrderBillingLink = missionOrderBillingLink;
        this.billingFrequency = billingFrequency;
        this.warningPoint = warningPoint;
        this.description = description;
        this.bpu = bpu;
        this.marketEstablishments = marketEstablishments;
        this.marketPeoples = marketPeoples;
        this.marketUsers = marketUsers;
        this.agency = agency;
        this.facturationAnalysis = facturationAnalysis;
    }

    @Override
    public String getId() {
        return uuid;
    }
}
