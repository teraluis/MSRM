package markets;

import java.util.Objects;

public class SimpleMarketPeople {
    public String people;
    public String role;

    public SimpleMarketPeople(
            String people,
            String role
    ) {
        this.people = people;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleMarketPeople)) return false;
        SimpleMarketPeople that = (SimpleMarketPeople) o;
        return people.equals(that.people);
    }

    @Override
    public int hashCode() {
        return Objects.hash(people);
    }
}
