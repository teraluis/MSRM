package api.v1.forms;

import java.math.BigDecimal;

public class OrderLineForm {

    public OrderLineForm() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(final String uuid) {
        this.uuid = uuid;
    }

    public String getRefadx() {
        return refadx;
    }

    public void setRefadx(final String refadx) {
        this.refadx = refadx;
    }

    public String getRefbpu() {
        return refbpu;
    }

    public void setRefbpu(final String refbpu) {
        this.refbpu = refbpu;
    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(final BigDecimal price) {
        this.price = price;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(final Integer quantity) {
        this.quantity = quantity;
    }

    public Float getDiscount() {
        return discount;
    }

    public void setDiscount(final Float discount) {
        this.discount = discount;
    }

    public String getTvacode() {
        return tvacode;
    }

    public void setTvacode(final String tvacode) {
        this.tvacode = tvacode;
    }

    public Float getTotal() {
        return total;
    }

    public void setTotal(final Float total) {
        this.total = total;
    }

    protected String uuid;
    protected String refadx;
    protected String refbpu;
    protected String designation;
    protected BigDecimal price;
    protected Integer quantity;
    protected Float discount;
    protected String tvacode;
    protected Float total;
}
