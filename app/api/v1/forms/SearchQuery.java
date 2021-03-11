package api.v1.forms;

import javax.validation.constraints.NotNull;

public class SearchQuery {

    @NotNull
    private String query;

    public SearchQuery() {
        // Nothing to do here
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }
}
