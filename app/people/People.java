package people;

import core.Single;
import core.models.AddressWithRole;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class People implements Single<String> {

    public final String uuid;
    public final String title;
    public final String lastname;
    public final String firstname;
    public final Optional<String> workMail;
    public final Optional<String> email;
    public final Optional<String> workPhone;
    public final Optional<String> mobilePhone;
    public final Optional<String> jobDescription;

    public People(final Optional<String> uuid, final String title, final String lastname, final String firstname,
                  final Optional<String> workMail, final Optional<String> email, final Optional<String> workPhone,
                  final Optional<String> mobilePhone, final Optional<String> jobDescription) {
        this.uuid = uuid.orElseGet(() -> "people-" + UUID.randomUUID());
        this.title = title;
        this.lastname = lastname;
        this.firstname = firstname;
        this.workMail = workMail;
        this.email = email;
        this.workPhone = workPhone;
        this.mobilePhone = mobilePhone;
        this.jobDescription = jobDescription;
    }

    @Override
    public String getId() {
        return uuid;
    }

    public core.models.People serialize(List<AddressWithRole> addresses) {
        return new core.models.People(uuid, title, lastname, firstname, workMail.orElse(null), email.orElse(null), workPhone.orElse(null), mobilePhone.orElse(null), jobDescription.orElse(null), addresses);
    }

}
