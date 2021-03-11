package core.search;

import java.io.Serializable;
import java.util.List;

public interface Indexable extends Serializable {
    String getId();
    String getType();
    String getTypeLabel();
}
