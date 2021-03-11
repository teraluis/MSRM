package api.v1.forms;

import java.math.BigDecimal;
import java.util.List;

public class AddBillLineForm {

    public AddBillLineForm() {
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

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(final BigDecimal discount) {
        this.discount = discount;
    }

    public String getTvacode() {
        return tvacode;
    }

    public void setTvacode(final String tvacode) {
        this.tvacode = tvacode;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(final BigDecimal total) {
        this.total = total;
    }

    public String getCreditnote() {
        return creditnote;
    }

    public void setCreditnote(String creditnote) {
        this.creditnote = creditnote;
    }

    public List<String> getPrestations() {
        return prestations;
    }

    public void setPrestations(List<String> prestations) {
        this.prestations = prestations;
    }

    protected String refadx;
    protected String refbpu;
    protected String designation;
    protected BigDecimal price;
    protected Integer quantity;
    protected BigDecimal discount;
    protected String tvacode;
    protected BigDecimal total;
    protected String creditnote;
    protected List<String> prestations;

}
