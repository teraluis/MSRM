package core.search;

public class PaginatedResult<T> {
    final int from;
    final int size;
    final int total;
    final T data;

    public PaginatedResult(final int from, final int size, final int total, final T data) {
        this.from = from;
        this.size = size;
        this.total = total;
        this.data = data;
    }

    public int getFrom() {
        return from;
    }

    public int getSize() {
        return size;
    }

    public int getTotal() {
        return total;
    }

    public T getData() {
        return data;
    }
}
