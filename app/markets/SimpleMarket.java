package markets;

import core.Single;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

public class SimpleMarket implements Single<String> {

    public final String uuid;
    public final String name;
    public final String marketNumber;
    public final Optional<String> status;
    public final String customerRequirement;
    public final String agency;
    public final String tenant;
    public final String facturationAnalysis;
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

    public SimpleMarket(
            final Optional<String> uuid,
            final String name,
            String marketNumber,
            final Optional<String> status,
            final String customerRequirement,
            final String agency,
            final String tenant,
            final String facturationAnalysis,
            final Optional<LocalDate> receiveDate,
            final Optional<LocalDate> responseDate,
            final Optional<LocalDate> returnDate,
            final Optional<LocalDate> startDate,
            final Optional<Integer> duration,
            final Optional<String> publicationNumber,
            final Optional<String> origin,
            final Optional<String> estimateVolume,
            final Optional<String> missionOrderType,
            final Optional<String> deadlineModality,
            final Optional<String> dunningModality,
            final Optional<String> interventionCondition,
            final Optional<String> specificReportNaming,
            final Optional<String> specificReportDisplay,
            final Optional<String> specificBilling,
            final Optional<String> missionOrderBillingLink,
            final Optional<String> billingFrequency,
            final Optional<String> warningPoint,
            final Optional<String> description
    ) {
        this.uuid = uuid.orElseGet(() -> "market-" + UUID.randomUUID());
        this.name = name;
        this.marketNumber = marketNumber;
        this.status = status;
        this.customerRequirement = customerRequirement;
        this.agency = agency;
        this.tenant = tenant;
        this.facturationAnalysis = facturationAnalysis;
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
    }

    @Override
    public String getId() {
        return uuid;
    }
}
