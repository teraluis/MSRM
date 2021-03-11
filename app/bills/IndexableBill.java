package bills;

import core.search.Indexable;

import java.util.Date;
import java.util.Optional;

public class IndexableBill implements Indexable {
    private String id;
    private OrderForBillIndexable order;
    private String name;
    private String creditNote;
    private AccountForIndexableBill account;
    private String market;
    private String address;
    private String status;
    private Long exportDate;

    public IndexableBill() {
    }

    public IndexableBill(String id, OrderForBillIndexable order, String name, String creditNote, AccountForIndexableBill account, String market, String address, String status, Optional<Date> exportDate) {
        this.id = id;
        this.order = order;
        this.name = name;
        this.creditNote = creditNote;
        this.account = account;
        this.market = market;
        this.address = address;
        this.status = status;
        this.exportDate = exportDate.map(d -> d.toInstant().toEpochMilli()).orElse(null);
    }

    @Override
    public String getId() {
        return this.id;
    }

    public IndexableBill setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public String getType() {
        return "bill";
    }

    public void setType(String type) {
        // Nothing here
    }

    @Override
    public String getTypeLabel() {
        return "facture";
    }

    public void setTypeLabel(String label) {
        // Nothing here
    }

    public OrderForBillIndexable getOrder() {
        return order;
    }

    public IndexableBill setOrder(OrderForBillIndexable order) {
        this.order = order;
        return this;
    }

    public String getName() {
        return name;
    }

    public IndexableBill setName(String name) {
        this.name = name;
        return this;
    }

    public String getCreditNote() {
        return creditNote;
    }

    public IndexableBill setCreditNote(String creditNote) {
        this.creditNote = creditNote;
        return this;
    }

    public AccountForIndexableBill getAccount() {
        return account;
    }

    public IndexableBill setAccount(AccountForIndexableBill account) {
        this.account = account;
        return this;
    }

    public String getMarket() {
        return market;
    }

    public IndexableBill setMarket(String market) {
        this.market = market;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public IndexableBill setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getStatus() {
        return status;
    }

    public IndexableBill setStatus(String status) {
        this.status = status;
        return this;
    }

    public Long getExportDate() {
        return exportDate;
    }

    public IndexableBill setExportDate(Long exportDate) {
        this.exportDate = exportDate;
        return this;
    }
}

