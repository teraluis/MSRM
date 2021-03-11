package utils.VariablesExport;

public class PaymentTypeExport {

    public final String number;
    public final String code;
    public final String recovery;

    public PaymentTypeExport(final String number, final String code, final String recovery) {
        this.number = number;
        this.code = code;
        this.recovery = recovery;
    }
}
