package bills;

public class OrderForBillIndexable {
    private String name;
    private String id;
    private String referenceNumber;

    public String getName() {
        return name;
    }

    public OrderForBillIndexable setName(String name) {
        this.name = name;
        return this;
    }

    public String getId() {
        return id;
    }

    public OrderForBillIndexable setId(String id) {
        this.id = id;
        return this;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public OrderForBillIndexable setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
        return this;
    }
}
