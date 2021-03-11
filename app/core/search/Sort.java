package core.search;

public class Sort {
    private SortDirection direction;
    private String[] field;

    public Sort() {
    }

    public Sort(SortDirection direction, String[] field) {
        this.direction = direction;
        this.field = field;
    }

    public SortDirection getDirection() {
        return direction;
    }

    public void setDirection(SortDirection direction) {
        this.direction = direction;
    }

    public String getDirectionString() {
        return direction == SortDirection.ASC ? "asc" : "desc";
    }

    public String[] getField() {
        return field;
    }

    public void setField(String[] field) {
        this.field = field;
    }
}
