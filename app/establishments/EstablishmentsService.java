package establishments;

import addresses.AddressWithRole;
import core.search.Pageable;
import people.PeopleWithRole;
import core.search.PaginatedResult;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface EstablishmentsService {

    CompletionStage<Optional<String>> add(String organization, Establishment establishment, Optional<String> login);

    CompletionStage<Optional<Establishment>> update(String organization, Establishment establishment, Optional<String> login);

    CompletionStage<Optional<Establishment>> get(String organization, String uuid);

    CompletionStage<Optional<Establishment>> getFromSiret(String organization, String siret);

    CompletionStage<List<Establishment>> getAll(String organization);

    CompletionStage<List<Establishment>> getFromEntity(String organization, String entity);

    CompletionStage<List<Establishment>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<String> getSageCode(String organization);

    CompletionStage<List<Establishment>> search(String organization, String pattern);

    CompletionStage<List<Establishment>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<api.v1.models.Establishment> serialize(String organization, Establishment establishment);

    CompletionStage<api.v1.models.FullEstablishment> serializeFull(String organization, Establishment establishment , Boolean hasOrder);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<List<EstablishmentWithRole>> getDelegatesByRole(String organization, String establishmentId, String role);

    CompletionStage<List<EstablishmentWithRole>> getDelegates(String organization, String establishmentId);

    CompletionStage<List<AddressWithRole>> getAddressesByRole(String organization, String establishmentId, String role);

    CompletionStage<List<AddressWithRole>> getAddresses(String organization, String establishmentId);

    CompletionStage<List<PeopleWithRole>> getPeopleByRole(String organization, String establishmentId, String role);

    CompletionStage<List<PeopleWithRole>> getPeople(String organization, String establishmentId);

    CompletionStage<Boolean> setClientExported(String organization, String establishmentId);

    CompletionStage<Boolean> setValidatorExported(String organization, String establishmentId);

    CompletionStage<Boolean> setClientUpToDate(String organization, String establishmentId);

    CompletionStage<Boolean> setValidatorUpToDate(String organization, String establishmentId);

    CompletionStage<Boolean> addDelegate(String organization, String establishmentId, String delegateId, String role, Optional<String> login);

    CompletionStage<Boolean> addAddress(String organization, String establishmentId, String addressId, String role, Optional<String> login);

    CompletionStage<Boolean> addPeople(String organization, String establishmentId, String peopleId, String role, Optional<String> login);

    CompletionStage<Boolean> removeDelegate(String organization, String establishmentId, String delegateId, String role, Optional<String> login);

    CompletionStage<Boolean> removeAddress(String organization, String establishmentId, String addressId, String role, Optional<String> login);

    CompletionStage<Boolean> removePeople(String organization, String establishmentId, String peopleId, String role, Optional<String> login);

    CompletionStage<Boolean> reindex(String organization);

    CompletionStage<PaginatedResult<List<IndexableEstablishment>>> getOverviews(final String organization, Pageable pageable);

    CompletionStage<List<EstablishmentComment>> getComments(final String organization, final String uuid);

    CompletionStage<Optional<EstablishmentComment>> addComment(final String organization, final EstablishmentComment comment);

    CompletionStage<Optional<api.v1.models.EstablishmentComment>> serializeComment(String organization, EstablishmentComment comment);

    CompletionStage<IndexableEstablishment> buildIndexableEstablishment(String organization, Establishment establishment);
}
