package core.search;

import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Pageable {
    private Integer page;
    private Integer size;
    private List<FieldSortBuilder> sort = new ArrayList<>();
    private Optional<String> filter = Optional.empty();

    public Pageable(Map<String, String[]> entries) {
        if (entries.containsKey("pageNumber")) {
            setPage(Integer.parseInt(entries.get("pageNumber")[0]));
        }

        if (entries.containsKey("pageSize")) {
            setSize(Integer.parseInt(entries.get("pageSize")[0]));
        }

        if (entries.containsKey("q")) {
            this.setFilter(Optional.ofNullable(entries.get("q")[0]));
        }

        if (entries.containsKey("sort_by")) {
            // TODO checker si y'a pas une lib qui fait ça
            Matcher m = Pattern.compile("((asc|desc)\\(([\\w,\\.]*,?)*\\),?)")
                    .matcher(entries.get("sort_by")[0]);
            while (m.find()) {
                String match = m.group();
                Matcher direction = Pattern.compile("^(asc|desc)").matcher(match);
                Matcher value = Pattern.compile("\\(([^)]+)\\)").matcher(match);
                if (direction.find() && value.find()) {
                    String v = value.group(0);

                    addSort(
                            Arrays.stream(v.substring(v.indexOf("(") + 1, v.indexOf(")")).split(",")).map(s ->
                                    new FieldSortBuilder(s).order("asc".equals(direction.group(0)) ? SortOrder.ASC : SortOrder.DESC)
                            ).collect(Collectors.toList())
                    );
                }
            }
        }
    }

    public Integer getPage() {
        return page != null ? page : 0;
    }

    public Pageable setPage(Integer page) {
        this.page = page;
        return this;
    }

    public Integer getSize() {
        return size != null ? size : 10;
    }

    public Pageable setSize(Integer size) {
        this.size = size;
        return this;
    }

    public List<FieldSortBuilder> getSort() {
        return sort;
    }

    public Pageable setSort(List<FieldSortBuilder> sort) {
        this.sort = sort;
        return this;
    }

    public Pageable addSort(FieldSortBuilder sort) {
        this.sort.add(sort);
        return this;
    }

    public Pageable addSort(List<FieldSortBuilder> sort) {
        this.sort.addAll(sort);
        return this;
    }

    public Optional<String> getFilter() {
        return filter;
    }

    public Pageable setFilter(Optional<String> filter) {
        this.filter = filter;
        return this;
    }
}

