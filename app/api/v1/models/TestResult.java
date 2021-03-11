package api.v1.models;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

public class TestResult {
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final String orderId;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final String orderName;
    @JsonInclude(JsonInclude.Include.NON_ABSENT)
    public final List<String> errors;

    public TestResult(String orderId, String orderName, List<String> errors) {
        this.orderId = orderId;
        this.orderName = orderName;
        this.errors = errors;
    }
}
