package markets;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import core.models.People;

import java.util.Objects;

public class MarketPeople {
    public People people;
    public String role;

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public MarketPeople(
            @JsonProperty("people")
                    People people,
            @JsonProperty("role")
                    String role
    ) {
        this.people = people;
        this.role = role;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MarketPeople)) return false;
        MarketPeople that = (MarketPeople) o;
        return people.equals(that.people);
    }

    @Override
    public int hashCode() {
        return Objects.hash(people);
    }
}
