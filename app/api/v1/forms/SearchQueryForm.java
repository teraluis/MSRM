package api.v1.forms;

import play.data.validation.Constraints;

public class SearchQueryForm {

    public SearchQueryForm() {
        // Nothing to do here
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(final String query) {
        this.query = query;
    }

    @Constraints.Required
    String query;
}
