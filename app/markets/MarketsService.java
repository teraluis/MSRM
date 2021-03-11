package markets;

import api.v1.models.FullMarket;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface MarketsService {

    CompletionStage<Optional<String>> add(String organization, SimpleMarket market);

    CompletionStage<List<SimpleMarket>> getAll(String organization);

    CompletionStage<List<SimpleMarket>> getFromAccount(String organization, String accountId);

    CompletionStage<List<SimpleMarket>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<SimpleMarket>> search(String organization, String pattern);

    CompletionStage<List<SimpleMarket>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<FullMarket> serialize(String organization, SimpleMarket market);

    CompletionStage<Optional<SimpleMarket>> get(String organization, String uuid);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

    CompletionStage<Optional<String>> addMarket(String organization, Market market);

    CompletionStage<Boolean> addContact(MarketPeople marketPeople, String uuid);

    CompletionStage<List<SimpleMarketPeople>> getContact(String uuid);
    CompletionStage<List<SimpleMarketPeople>> getPeopleByRole(String organization, String marketId, String role);

    CompletionStage<Void> updateContact(MarketPeople marketPeople, String uuid, String peopleUuid, String oldRole);

    CompletionStage<Void> deleteContact(String uuid, String peopleUuid, String role);

    CompletionStage<api.v1.models.MarketPeople> serializeMarketPeople(String organization, SimpleMarketPeople marketPeople);

    CompletionStage<Boolean> addAccount(MarketEstablishment marketEstablishment, String uuid);

    CompletionStage<List<SimpleMarketEstablishment>> getEstablishment(String uuid);

    CompletionStage<Void> updateAccount(MarketEstablishment marketEstablishment, String uuid, String accountUuid, String organization);

    CompletionStage<Void> deleteAccount(String uuid, String accountUuid, String role);

    CompletionStage<api.v1.models.MarketEstablishment> serializeMarketEstablishment(String organization, SimpleMarketEstablishment marketAccount);

    CompletionStage<Void> addUser(MarketUser marketUser, String uuid);

    CompletionStage<List<SimpleMarketUser>> getUser(String uuid);

    CompletionStage<Void> updateUser(MarketUser marketUser, String uuid, String userUuid, String organization);

    CompletionStage<Void> deleteUser(String uuid, String userLogin);

    CompletionStage<api.v1.models.MarketUser> serializeMarketUser(String organization, SimpleMarketUser marketUser);

    CompletionStage<Void> updateMarket(Market market);

    CompletionStage<Optional<String>> addReferenceIfNotExist(String uuid, BpuReference reference);

    CompletionStage<List<BpuReference>> getReferences(String uuid, String reference);

    CompletionStage<List<BpuReference>> getReferencesFromDesignation(String uuid, String designation);

    CompletionStage<Optional<Bpu>> getBpu(String uuid);

    CompletionStage<Bpu> addBpu(String organization, Bpu bpu);

    CompletionStage<Void> deleteBpu(String uuid);

    CompletionStage<List<MarketComment>> getComments(String organization, String uuid);

    CompletionStage<Optional<MarketComment>> addComment(String organization, MarketComment marketComment);

    CompletionStage<Optional<api.v1.models.MarketComment>> serializeComment(String organization, MarketComment comment);
}
