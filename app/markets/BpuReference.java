package markets;

import java.math.BigDecimal;
import java.util.Optional;

public class BpuReference {

    public final Optional<String> reference;
    public final Optional<String> designation;
    public final BigDecimal price;


    public BpuReference(Optional<String> reference, Optional<String> designation, BigDecimal price) {
        this.reference = reference;
        this.designation = designation;
        this.price = price;
    }
}
