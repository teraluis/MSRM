package people;

import org.apache.commons.text.similarity.LevenshteinDistance;

import java.util.Comparator;

public class SearchPeopleComparator implements Comparator<People> {

    private String pattern;

    public SearchPeopleComparator(final String pattern) {
        this.pattern = pattern;
    }

    @Override
    public int compare(People o1, People o2) {
        Integer o1value = LevenshteinDistance.getDefaultInstance().apply(pattern, o1.firstname + " " + o1.lastname);
        Integer o2value = LevenshteinDistance.getDefaultInstance().apply(pattern, o2.firstname + " " + o2.lastname);
        return o1value - o2value;
    }
}
