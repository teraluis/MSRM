package utils.VariablesExport;

import java.util.HashMap;
import java.util.Map;

public class PaymentTypeExportUtils {

    private final static Map<String, PaymentTypeExport> typeMap = new HashMap<String, PaymentTypeExport>() {
        {
            put("vir", new PaymentTypeExport("24", "VIR", "511400"));
        }
    };

    public static Map<String, PaymentTypeExport> paymentTypeMap() {
        return typeMap;
    }

}
