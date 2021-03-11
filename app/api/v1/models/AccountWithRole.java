package api.v1.models;

public class AccountWithRole {

    public Account account;
    public String role;

    public AccountWithRole(Account account, String role) {
        this.account = account;
        this.role = role;
    }

    public static AccountWithRole serialize(Account account, String role) {
        return new AccountWithRole(account, role);
    }
}
