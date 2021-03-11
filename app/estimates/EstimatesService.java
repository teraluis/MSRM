package estimates;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface EstimatesService {

    CompletionStage<Optional<String>> add(String organization, Estimate estimate);

    CompletionStage<List<Estimate>> getAll(String organization);

    CompletionStage<List<Estimate>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<List<Estimate>> search(String organization, String pattern);

    CompletionStage<List<Estimate>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<Optional<Estimate>> get(String organization, String uuid);

    CompletionStage<Optional<api.v1.models.Estimate>> getFullEstimate(String organization, String uuid);

    CompletionStage<List<Estimate>> getFromMarket(String organization, String marketUuid);

    CompletionStage<List<Estimate>> getFromAccount(String organization, String accountUuid);

    CompletionStage<Boolean> patch(String organization, String uuid, String name, Optional<String> market, Optional<String> account);

    CompletionStage<Optional<String>> delete(String organization, String uuid);

}
