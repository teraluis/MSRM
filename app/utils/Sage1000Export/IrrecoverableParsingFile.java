package utils.Sage1000Export;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class IrrecoverableParsingFile {
    protected final static Logger logger = LoggerFactory.getLogger(IrrecoverableParsingFile.class);

    public static List<String> parseIrrecoverableFile(List<List<String>> file) {
        List<String> billNames = new ArrayList<>();
        for (List<String> line : file) {
            if (line.size() > 1) {
                billNames.add(line.get(1));
            }
        }
        return billNames;
    }
}
