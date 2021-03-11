package orders;

import core.models.Address;
import core.search.Indexable;
import establishments.IndexableEstablishment;
import users.User;

import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class IndexableOrder implements Indexable {
    public final static String[] SEARCHABLE_FIELDS = {"id", "name", "status", "created", "purchaser.id", "purchaser.name", "account.id", "account.name", "market.id", "market.name", "description", "referenceNumber"};
    protected String id;
    protected String name;
    protected String status;
    protected Long created;
    protected IdWithName purchaser;
    protected Optional<IdWithName> account;
    protected Optional<IdWithName> market;
    protected Optional<String> description;
    protected Optional<String> referenceNumber;
    protected int nbEstate;
    protected IndexableEstablishment establishment;
    protected String commercial;
    protected String address;
    protected Long receive;
    protected Long delivery;
    protected Long visit;
    protected Long assessment;
    protected Set<String> estateRefs;
    protected Map<String, Integer> prestations;

    public IndexableOrder(final String id,
                          final String name,
                          final String status,
                          final Date created,
                          final IdWithName purchaser,
                          final Optional<IdWithName> account,
                          final Optional<IdWithName> market,
                          final Optional<String> description,
                          final Optional<String> referenceNumber,
                          final Optional<IndexableEstablishment> establishment,
                          final Optional<User> commercial,
                          final Optional<Address> address,
                          final Optional<Date> receive,
                          final Optional<Date> delivery,
                          final Optional<Date> visit,
                          final Optional<Date> assessment,
                          final Set<String> estateRefs,
                          final Map<String, Integer> prestations) {
        this.id = id;
        this.name = name;
        this.status = status;
        this.created = created.toInstant().toEpochMilli();
        this.purchaser = purchaser;
        this.account = account;
        this.market = market;
        this.description = description;
        this.referenceNumber = referenceNumber;
        this.establishment = establishment.orElseGet(null);
        this.commercial = commercial.map(user -> user.last_name + " " + user.first_name).orElse("");
        this.address = buildAddress(address);
        this.receive = receive.map(d -> d.toInstant().toEpochMilli()).orElse(null);
        this.delivery = delivery.map(d -> d.toInstant().toEpochMilli()).orElse(null);
        this.visit = visit.map(d -> d.toInstant().toEpochMilli()).orElse(null);
        this.assessment = assessment.map(d -> d.toInstant().toEpochMilli()).orElse(null);
        this.estateRefs = estateRefs;
        this.nbEstate = estateRefs.size();
        this.prestations = prestations;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getType() {
        return "order";
    }

    @Override
    public String getTypeLabel() {
        return "commande";
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }

    public Long getCreated() {
        return created;
    }

    public IdWithName getPurchaser() {
        return purchaser;
    }

    public Optional<IdWithName> getAccount() {
        return account;
    }

    public Optional<IdWithName> getMarket() {
        return market;
    }

    public Optional<String> getDescription() {
        return description;
    }

    public Optional<String> getReferenceNumber() {
        return referenceNumber;
    }

    public IndexableOrder() {
    }

    public void setId(final String id) {
        this.id = id;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public void setCreated(final Long created) {
        this.created = created;
    }

    public void setPurchaser(final IdWithName purchaser) {
        this.purchaser = purchaser;
    }

    public void setAccount(final IdWithName account) {
        this.account = Optional.ofNullable(account);
    }

    public void setMarket(final IdWithName market) {
        this.market = Optional.ofNullable(market);
    }

    public void setDescription(final String description) {
        this.description = Optional.ofNullable(description);
    }

    public void setReferenceNumber(final String referenceNumber) {
        this.referenceNumber = Optional.ofNullable(referenceNumber);
    }

    // Needed to parse JSON
    public void setTypeLabel(final String typeLabel) {
        // Nothing to do here
    }

    // Needed to parse JSON
    public void setType(final String type) {
        // Nothing to do here
    }

    public int getNbEstate() {
        return nbEstate;
    }

    public void setNbEstate(int nbEstate) {
        this.nbEstate = nbEstate;
    }

    public IndexableEstablishment getEstablishment() {
        return establishment;
    }

    public void setEstablishment(IndexableEstablishment establishment) {
        this.establishment = establishment;
    }

    public String getCommercial() {
        return commercial;
    }

    public void setCommercial(String commercial) {
        this.commercial = commercial;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Long getReceive() {
        return receive;
    }

    public void setReceive(Long receive) {
        this.receive = receive;
    }

    public Long getDelivery() {
        return delivery;
    }

    public void setDelivery(Long delivery) {
        this.delivery = delivery;
    }

    public Long getVisit() {
        return visit;
    }

    public void setVisit(Long visit) {
        this.visit = visit;
    }

    public Long getAssessment() {
        return assessment;
    }

    public void setAssessment(Long assessment) {
        this.assessment = assessment;
    }

    public Set<String> getEstateRefs() {
        return estateRefs;
    }

    public void setEstateRefs(Set<String> estateRefs) {
        this.estateRefs = estateRefs;
    }

    public Map<String, Integer> getPrestations() {
        return prestations;
    }

    public void setPrestations(Map<String, Integer> prestations) {
        this.prestations = prestations;
    }

    private String buildAddress(Optional<Address> address) {
        if (!address.isPresent()) {
            return "";
        }
        Address add = address.get();

        if (add.gpsCoordinates.isPresent()) {
            return "GPS: " + add.gpsCoordinates.get();
        }

        if (add.inseeCoordinates.isPresent()) {
            return "INSEE: " + add.inseeCoordinates.get();
        }

        StringBuilder result = new StringBuilder();

        add.address1.ifPresent(result::append);
        add.address2.ifPresent(a -> result.append(", ").append(a));
        add.postCode.ifPresent(a -> result.append(", ").append(a));
        add.city.ifPresent(a -> result.append(" ").append(a));

        return result.toString();
    }

    protected static class IdWithName {
        public String id;
        public String name;

        public IdWithName() {
        }

        public IdWithName(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }
}
