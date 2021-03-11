package estimates;

import markets.MarketsService;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class SimpleEstimatesService implements EstimatesService {

    protected final EstimatesRepository estimatesRepository;
    protected final MarketsService marketsService;

    @Inject
    public SimpleEstimatesService(EstimatesRepository estimatesRepository, MarketsService marketsService) {
        this.estimatesRepository = estimatesRepository;
        this.marketsService = marketsService;
    }

    @Override
    public CompletionStage<Optional<String>> add(String organization, Estimate estimate) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.add(organization, estimate));
    }

    @Override
    public CompletionStage<List<Estimate>> getAll(String organization) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.getAll(organization));
    }

    @Override
    public CompletionStage<List<Estimate>> getPage(String organization, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.getPage(organization, offset, length));
    }

    @Override
    public CompletionStage<List<Estimate>> search(String organization, String pattern) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.search(organization, pattern));
    }

    @Override
    public CompletionStage<List<Estimate>> searchPage(String organization, String pattern, Integer offset, Integer length) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.searchPage(organization, pattern, offset, length));
    }

    @Override
    public CompletionStage<Optional<Estimate>> get(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.get(organization, uuid));
    }

    @Override
    public CompletionStage<Optional<api.v1.models.Estimate>> getFullEstimate(String organization, String uuid) {
        return get(organization, uuid).thenCompose(estimate -> {
            if (estimate.isPresent()) {
                if (estimate.get().market.isPresent()) {
                    return marketsService.get(organization, estimate.get().market.get()).thenCompose(market -> {
                        if (market.isPresent()) {
                            return marketsService.serialize(organization, market.get()).thenApply(finalMarket -> Optional.of(api.v1.models.Estimate.serialize(estimate.get(), Optional.of(finalMarket), Optional.empty())));
                        } else {
                            return CompletableFuture.completedFuture(Optional.of(api.v1.models.Estimate.serialize(estimate.get(), Optional.empty(), Optional.empty())));
                        }
                    });
                } else {
                    // TODO
                    return CompletableFuture.completedFuture(Optional.<api.v1.models.Estimate>empty());
                }
            } else {
                return CompletableFuture.completedFuture(Optional.<api.v1.models.Estimate>empty());
            }
        });
    }

    @Override
    public CompletionStage<List<Estimate>> getFromMarket(String organization, String marketUuid) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.getFromMarket(organization, marketUuid));
    }

    @Override
    public CompletionStage<List<Estimate>> getFromAccount(String organization, String accountUuid) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.getFromAccount(organization, accountUuid));
    }

    @Override
    public CompletionStage<Boolean> patch(String organization, String uuid, String name, Optional<String> market, Optional<String> account) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.patch(organization, uuid, name, market, account));
    }

    @Override
    public CompletionStage<Optional<String>> delete(String organization, String uuid) {
        return CompletableFuture.supplyAsync(() -> this.estimatesRepository.delete(organization, uuid));
    }
}
