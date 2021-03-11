package core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CsvLine {

    final private HashMap<String, String> values;

    protected CsvLine(final HashMap<String, String> values) {
        this.values = values;
    }

    public String generateLine(String[] columns) {
        List<String> finalValues = new ArrayList<>();
        for (String column : columns) {
            finalValues.add(values.get(column));
        }
        return String.join("\t", finalValues) + "\n";
    }

}
