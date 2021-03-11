package agencies;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public interface AgenciesService {

    CompletionStage<Optional<String>> add(String organization, Agency activity);

    CompletionStage<Optional<Agency>> get(String organization, String uuid);

    CompletionStage<List<Agency>> getAll(String organization);

    CompletionStage<List<Agency>> getPage(String organization, Integer offset, Integer length);

    CompletionStage<Optional<Agency>> getFromOfficeName(String organization, String officeName);

    CompletionStage<List<Agency>> search(String organization, String pattern);

    CompletionStage<List<Agency>> searchPage(String organization, String pattern, Integer offset, Integer length);

    CompletionStage<api.v1.models.Agency> serialize(String organization, Agency agency);

    CompletionStage<Optional<Agency>> update(String organization, Agency agency);

    CompletionStage<Optional<String>> delete(String organization, String uuid);
}
