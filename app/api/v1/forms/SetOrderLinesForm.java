package api.v1.forms;

import java.util.List;

public class SetOrderLinesForm {

    public SetOrderLinesForm() {
    }

    public List<OrderLineForm> getOrderLines() {
        return orderLines;
    }

    public void setOrderLines(List<OrderLineForm> orderLines) {
        this.orderLines = orderLines;
    }

    protected List<OrderLineForm> orderLines;
}
