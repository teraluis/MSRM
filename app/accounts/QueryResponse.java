package accounts;

import java.util.List;

public class QueryResponse<T> {
    private List<T> data;
    private Integer total;

    public List<T> getData() {
        return data;
    }

    public QueryResponse<T> setData(List<T> data) {
        this.data = data;
        return this;
    }

    public Integer getTotal() {
        return total;
    }

    public QueryResponse<T> setTotal(Integer total) {
        this.total = total;
        return this;
    }
}
