package api.v1.models;

import core.models.People;

public class MarketPeople {
    public final People people;
    public final String role;

    public MarketPeople(People people, String role) {
        this.people = people;
        this.role = role;
    }

    public static MarketPeople serialize(People people, String role) {
        return new MarketPeople(people, role);
    }
}
