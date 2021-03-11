package api.v1.models;

import core.models.AddressWithRole;
import core.models.PeopleWithRole;

import java.util.List;

public class FullEstablishment {

    public final Establishment establishment;
    public final Account account;
    public final List<AddressWithRole> addresses;
    public final List<EstablishmentWithRole> delegates;
    public final List<PeopleWithRole> contacts;
    public final Boolean hasOrders; // not used in exports

    public FullEstablishment(final Establishment establishment, final Account account,
                             final List<AddressWithRole> addresses, final List<EstablishmentWithRole> delegates,
                             final List<PeopleWithRole> contacts, final Boolean hasOrders) {
        this.establishment = establishment;
        this.account = account;
        this.addresses = addresses;
        this.delegates = delegates;
        this.contacts = contacts;
        this.hasOrders = hasOrders;
    }
}
