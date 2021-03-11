package filter;

import org.apache.commons.lang3.NotImplementedException;

public class SearchCriteria {
    private String key;
    private SearchOperation operation;
    private Object value;

    public SearchCriteria(String key, SearchOperation operation, Object value) {
        this.key = key;
        this.operation = operation;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public SearchCriteria setKey(String key) {
        this.key = key;
        return this;
    }

    public SearchOperation getOperation() {
        return operation;
    }

    public SearchCriteria setOperation(SearchOperation operation) {
        this.operation = operation;
        return this;
    }

    public Object getValue() {
        return value;
    }

    public SearchCriteria setValue(Object value) {
        this.value = value;
        return this;
    }

    public String getCriteriaOperationAndValueString() {
        switch (operation) {
            case STARTS_WITH:
            case LIKE:
            case CONTAINS:
            case ENDS_WITH:
                return "ILIKE ?";
            case GREATER_THAN:
            case LESS_THAN:
            case EQUALITY:
                return "= ?";
            default:
                throw new NotImplementedException("Not implemented yet");
        }
    }
}
