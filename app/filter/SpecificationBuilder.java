package filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class SpecificationBuilder {
    protected static final Logger log = LoggerFactory.getLogger(SpecificationBuilder.class);
    private List<SearchCriteria> params = new ArrayList<>();

    public SpecificationBuilder with(String key, String operation, Object value, String prefix, String suffix) {
        SearchOperation op = SearchOperation.getSimpleOperation(operation.charAt(0));

        if (op != null) {
            if (op == SearchOperation.EQUALITY) {
                boolean startWithAsterisk = prefix.contains("*");
                boolean endWithAsterisk = suffix.contains("*");

                if (startWithAsterisk && endWithAsterisk) {
                    op = SearchOperation.CONTAINS;
                } else if (startWithAsterisk) {
                    op = SearchOperation.STARTS_WITH;
                } else if (endWithAsterisk) {
                    op = SearchOperation.ENDS_WITH;
                }
            }
            params.add(new SearchCriteria(key, op, value));
        }
        return this;
    }

    public List<SearchCriteria> getParams() {
        return params;
    }
}
