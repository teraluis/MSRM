package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import core.models.People;
import markets.Bpu;
import markets.MarketEstablishmentRole;
import markets.MarketPeopleRole;
import markets.MarketUserRole;
import users.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FullMarket {

    public String uuid;
    public String name;
    public String marketNumber;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public Optional<String> status;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public List<Bpu> bpu = new ArrayList<>();
    public List<MarketEstablishment> marketEstablishments = new ArrayList<>();
    public List<MarketPeople> marketPeoples = new ArrayList<>();
    public List<MarketUser> marketUsers = new ArrayList<>();
    public Agency agency;
    public String customerRequirement;
    public String tenant;
    public Optional<LocalDate> receiveDate;
    public Optional<LocalDate> responseDate;
    public Optional<LocalDate> returnDate;
    public Optional<LocalDate> startDate;
    public Optional<Integer> duration;
    public Optional<String> publicationNumber;
    public Optional<String> origin;
    public Optional<String> estimateVolume;
    public Optional<String> missionOrderType;
    public Optional<String> deadlineModality;
    public Optional<String> dunningModality;
    public Optional<String> interventionCondition;
    public Optional<String> specificReportNaming;
    public Optional<String> specificReportDisplay;
    public Optional<String> specificBilling;
    public Optional<String> missionOrderBillingLink;
    public Optional<String> billingFrequency;
    public Optional<String> warningPoint;
    public Optional<String> description;
    public String facturationAnalysis;

    public FullMarket() {
    }

    public FullMarket(
            String uuid,
            String name,
            String marketNumber,
            Optional<String> status,
            Agency agency,
            String tenant,
            String facturationAnalysis,
            Optional<LocalDate> receiveDate,
            Optional<LocalDate> responseDate,
            Optional<LocalDate> returnDate,
            Optional<LocalDate> startDate,
            Optional<Integer> duration,
            Optional<String> publicationNumber,
            Optional<String> origin,
            Optional<String> estimateVolume,
            Optional<String> missionOrderType,
            Optional<String> deadlineModality,
            Optional<String> dunningModality,
            Optional<String> interventionCondition,
            Optional<String> specificReportNaming,
            Optional<String> specificReportDisplay,
            Optional<String> specificBilling,
            Optional<String> missionOrderBillingLink,
            Optional<String> billingFrequency,
            Optional<String> warningPoint,
            Optional<String> description

    ) {
        this.uuid = uuid;
        this.name = name;
        this.marketNumber = marketNumber;
        this.status = status;
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

    public static FullMarket serialize(markets.SimpleMarket market) {
        FullMarket fullMarket = new FullMarket();
        fullMarket.uuid = market.uuid;
        fullMarket.name = market.name;
        fullMarket.marketNumber = market.marketNumber;
        fullMarket.status = market.status;
        fullMarket.customerRequirement = market.customerRequirement;
        fullMarket.tenant = market.tenant;
        fullMarket.facturationAnalysis = market.facturationAnalysis;
        fullMarket.receiveDate = market.receiveDate;
        fullMarket.responseDate = market.responseDate;
        fullMarket.returnDate = market.returnDate;
        fullMarket.startDate = market.startDate;
        fullMarket.duration = market.duration;
        fullMarket.publicationNumber = market.publicationNumber;
        fullMarket.origin = market.origin;
        fullMarket.estimateVolume = market.estimateVolume;
        fullMarket.missionOrderType = market.missionOrderType;
        fullMarket.deadlineModality = market.deadlineModality;
        fullMarket.dunningModality = market.dunningModality;
        fullMarket.interventionCondition = market.interventionCondition;
        fullMarket.specificReportNaming = market.specificReportNaming;
        fullMarket.specificReportDisplay = market.specificReportDisplay;
        fullMarket.specificBilling = market.specificBilling;
        fullMarket.missionOrderBillingLink = market.missionOrderBillingLink;
        fullMarket.billingFrequency = market.billingFrequency;
        fullMarket.warningPoint = market.warningPoint;
        fullMarket.description = market.description;
        return fullMarket;
    }

    public Optional<Establishment> getDefaultEstablishment() {
        return this.marketEstablishments
                .stream()
                .filter(marketEstablishment -> MarketEstablishmentRole.CLIENT.toString().equalsIgnoreCase(marketEstablishment.role))
                .map(marketEstablishment -> marketEstablishment.establishment)
                .findFirst();
    }

    public Optional<Establishment> getValidatorEstablishment() {
        return this.marketEstablishments
                .stream()
                .filter(marketEstablishment -> MarketEstablishmentRole.ADMINISTRATIVE_VALIDATOR.toString().equalsIgnoreCase(marketEstablishment.role))
                .map(marketEstablishment -> marketEstablishment.establishment)
                .findFirst();
    }

    public Optional<People> getDefaultContact() {
        return this.marketPeoples
                .stream()
                .filter(marketPeople -> MarketPeopleRole.KEY.toString().equalsIgnoreCase(marketPeople.role))
                .map(marketPeople -> marketPeople.people)
                .findFirst();
    }

    public Optional<User> getDefaultUser() {
        return this.marketUsers
                .stream()
                .filter(marketUser -> MarketUserRole.COMMERCIAL.toString().equalsIgnoreCase(marketUser.role))
                .map(marketUser -> marketUser.user)
                .findFirst();
    }

}
