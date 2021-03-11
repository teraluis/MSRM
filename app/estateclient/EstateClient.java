package estateclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;

import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class EstateClient {

    protected final WSClient wsClient;
    protected final Config config;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Inject
    public EstateClient(final WSClient wsClient, final Config config) {
        this.wsClient = wsClient;
        this.config = config;
    }

    public CompletionStage<Optional<Estate>> getEstate(final String organization, final String estateId) {

        final String baseRoute = this.config.getString("estate.url");
        final String route = baseRoute + "/v1/" + organization + "/estates/" + estateId;

        final WSRequest request = wsClient.url(route);

        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<Estate>() {
                }));
            } else {
                logger.error(wsResponse.getBody());
                return Optional.empty();
            }
        });
    }
}
