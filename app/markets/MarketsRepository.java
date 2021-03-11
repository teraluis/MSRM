package markets;

import java.util.List;
import java.util.Optional;

public interface MarketsRepository {

    Optional<String> add(String organization, SimpleMarket market);

    List<Bpu> getBpuByMarket(String market);

    Optional<Bpu> getBpu(String uuid);

    List<SimpleMarket> getAll(String organization);

    List<SimpleMarket> getFromEstablishment(String organization, String accountId);

    List<SimpleMarket> getPage(String organization, Integer offset, Integer length);

    List<SimpleMarket> search(String organization, String pattern);

    List<SimpleMarket> searchPage(String organization, String pattern, Integer offset, Integer length);

    Optional<SimpleMarket> get(String organization, String uuid);

    Optional<String> delete(String organization, String uuid);

    Optional<String> addMarket(String organization, Market market);

    Boolean addContact(MarketPeople marketPeople, String uuid);

    List<SimpleMarketPeople> getMarketPeopleByMarket(String uuid);
    List<SimpleMarketPeople> getPeopleByRole(String organization, String marketId, String role);

    void updateContact(MarketPeople marketPeople, String uuid, String peopleUuid, String oldRole);

    void deleteContact(String uuid, String peopleUuid, String role);

    Boolean addEstablishment(MarketEstablishment marketEstablishment, String uuid);

    List<SimpleMarketEstablishment> getMarketEstablishmentByMarket(String uuid);

    void updateEstablishment(MarketEstablishment marketEstablishment, String uuid, String accountUuid);

    void deleteAccount(String uuid, String accountUuid, String role);

    void addUser(MarketUser marketUser, String uuid);

    List<SimpleMarketUser> getMarketUserByMarket(String uuid);

    void updateUser(MarketUser marketUser, String uuid, String accountUuid);

    void deleteUser(String uuid, String userLogin);

    void updateMarket(Market market);

    Optional<String> addReferenceIfNotExist(String uuid, BpuReference reference);

    List<BpuReference> getReferences(String uuid, String searchString);

    List<BpuReference> getReferencesFromDesignation(String uuid, String searchString);

    Optional<String> addBpu(String organization, Bpu bpu);

    void deleteBpu(String uuid);

    List<MarketComment> getComments(String organization, String uuid);

    Optional<MarketComment> addComment(String organization, MarketComment marketComment);
}
