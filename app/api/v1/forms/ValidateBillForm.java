package api.v1.forms;

import java.util.List;

public class ValidateBillForm {

    public ValidateBillForm() {
    }

    public List<AddBillLineForm> getLignes() {
        return lignes;
    }

    public void setLignes(List<AddBillLineForm> lignes) {
        this.lignes = lignes;
    }

    public Long getDeadline() {
        return deadline;
    }

    public void setDeadline(Long deadline) {
        this.deadline = deadline;
    }

    protected List<AddBillLineForm> lignes;
    protected Long deadline;
}
