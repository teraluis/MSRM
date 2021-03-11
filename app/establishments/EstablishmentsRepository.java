package establishments;

import addresses.AddressWithRole;
import people.PeopleWithRole;

import java.util.List;
import java.util.Optional;

public interface EstablishmentsRepository {

    Optional<String> add(String organization, Establishment establishment);

    Optional<Establishment> update(String organization, Establishment establishment);

    Optional<Establishment> get(String organization, String uuid);

    Optional<Establishment> getFromSiret(String organization, String siret);

    List<Establishment> getAll(String organization);

    List<Establishment> getFromEntity(String organization, String entity);

    List<Establishment> getPage(String organization, Integer offset, Integer length);

    String getSageCode(String organization);

    List<Establishment> search(String organization, String pattern);

    List<Establishment> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<String> delete(String organization, String uuid);

    List<PeopleWithRole> getPeopleByRole(String organization, String establishmentId, String role);

    List<PeopleWithRole> getPeople(String organization, String establishmentId);

    List<AddressWithRole> getAddressesByRole(String organization, String establishmentId, String role);

    List<AddressWithRole> getAddresses(String organization, String establishmentId);

    List<EstablishmentWithRole> getDelegatesByRole(String organization, String establishmentId, String role);

    List<EstablishmentWithRole> getDelegates(String organization, String establishmentId);

    Boolean setClientExported(String organization, String establishmentId);

    Boolean setValidatorExported(String organization, String establishmentId);

    Boolean setClientUpToDate(String organization, String establishmentId);

    Boolean setValidatorUpToDate(String organization, String establishmentId);

    Boolean addDelegate(String establishmentId, String delegateId, String role);

    Boolean addAddress(String establishmentId, String addressId, String role);

    Boolean addPeople(String establishmentId, String peopleId, String role);

    Boolean removeDelegate(String establishmentId, String delegateId, String role);

    Boolean removeAddress(String establishmentId, String addressId, String role);

    Boolean removePeople(String establishmentId, String peopleId, String role);

    List<EstablishmentComment> getComments(String organization, String uuid);

    Optional<EstablishmentComment> addComment(String organization, EstablishmentComment comment);
}
