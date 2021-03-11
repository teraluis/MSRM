package api.v1.forms;

import java.util.List;

public class ListOverviewForm {
    List<Filter> filters;
    Sort sort;

    public ListOverviewForm() {
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(final List<Filter> filters) {
        this.filters = filters;
    }

    public Sort getSort() {
        return sort;
    }

    public void setSort(final Sort sort) {
        this.sort = sort;
    }

    public static class Filter {
        String value;
        String key;

        public Filter() {
        }

        public String getValue() {
            return value;
        }

        public void setValue(final String value) {
            this.value = value;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }
    }

    public static class Sort {
        String order;
        String key;

        public Sort() {
        }

        public String getOrder() {
            return order;
        }

        public void setOrder(final String order) {
            this.order = order;
        }

        public String getKey() {
            return key;
        }

        public void setKey(final String key) {
            this.key = key;
        }
    }
}
