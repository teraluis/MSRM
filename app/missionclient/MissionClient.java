package missionclient;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.typesafe.config.Config;
import core.models.AnalyseType;
import core.models.Prestation;
import core.models.TechnicalAct;
import missionclient.interventions.InterventionStatus;
import missionclient.interventions.InterventionUtils;
import missionclient.interventions.MaterializedIntervention;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.Json;
import play.libs.ws.WSClient;
import play.libs.ws.WSRequest;
import play.libs.ws.WSResponse;
import scala.Tuple2;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class MissionClient {

    protected final WSClient wsClient;
    protected final Config config;
    protected final String baseRoute;
    protected final String tenant = "ADX";
    protected final Logger logger = LoggerFactory.getLogger(MissionClient.class);


    @Inject
    public MissionClient(final WSClient wsClient, Config config) {
        this.wsClient = wsClient;
        this.config = config;
        this.baseRoute = config.getString("mission.url");
    }

    // POST     /v1/:orga/prestations       api.v1.controllers.PrestationsController.add(request: Request, orga)
    public CompletionStage<Optional<Prestation>> addPrestation(final AddPrestationForm prestation) {
        final String route = String.format("%s/v1/%s/prestations", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(prestation)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Prestation.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Associate result id to prestations (needs presta result to be created first)
    //    PATCH         /v1/:orga/prestations/:prestaId/result/:resultId        api.v1.controllers.PrestationsController.addResult(orga, prestaId, resultId)
    public CompletionStage<Boolean> addResultToPrestation(final String prestaId, final String resultId) {
        final String route = String.format("%s/v1/%s/prestations/%s/result", baseRoute, tenant, prestaId);
        final JsonNode resultIdJson = Json.parse(String.format("{\"id\": \"%s\"}", resultId));
        return wsClient.url(route).patch(resultIdJson).thenApply(wsResponse -> {
            return wsResponse.getStatus() < 300;
        });
    }

    // list of prestation from mission id
    //    GET           /v1/:orga/interventions/:uuid/prestations                    api.v1.controllers.PrestationsController.getByMissionId(orga, uuid)
    public CompletionStage<List<Prestation>> listPrestationByInterventionId(final String missionId) {
        final String route = String.format("%s/v1/%s/interventions/%s/prestations", baseRoute, tenant, missionId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<List<Prestation>>() {
                });
            } else {
                return new ArrayList<>();
            }
        });
    }

    // list of prestation from order id
    //    GET           /v1/:orga/prestations                    api.v1.controllers.PrestationsController.search(orga, uuid)
    public CompletionStage<List<Prestation>> listPrestationByOrderId(final String organization, final String orderId) {
        final String route = String.format("%s/v1/%s/prestations", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        request.addQueryParameter("order", orderId);
        return request.get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<List<Prestation>>() {
                });
            } else {
                return new ArrayList<>();
            }
        });
    }

    // list
    //    GET           /v1/:orga/prestationTypes                               api.v1.controllers.PrestationsController.getAllPrestationType(orga)
    public CompletionStage<List<TechnicalAct>> listPrestationType() {
        final String route = String.format("%s/v1/%s/prestationTypes", baseRoute, tenant);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<List<TechnicalAct>>() {
                });
            } else {
                return new ArrayList<>();
            }
        });
    }

    //    GET           /v1/:orga/prestationTypes/:id                           api.v1.controllers.PrestationsController.getPrestationType(orga, id)
    public CompletionStage<Optional<TechnicalAct>> getPrestationType(String id) {
        final String route = String.format("%s/v1/%s/prestationTypes/%s", baseRoute, tenant, id);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), TechnicalAct.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (asbestos)
    //    POST          /v1/:orga/results/asbestos                              api.v1.controllers.AsbestosResultController.add(orga, request: Request)
    public CompletionStage<Optional<String>> addAsbestosResult(final Asbestos asbestos) {
        final String route = String.format("%s/v1/%s/results/asbestos", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(asbestos)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/asbestos/:id                          api.v1.controllers.AsbestosResultController.get(orga, id)
    public CompletionStage<Optional<Asbestos>> getAsbestosResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/asbestos/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Asbestos.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (electricity)
    //    POST          /v1/:orga/results/electricity
    public CompletionStage<Optional<String>> addElectricityResult(final Electricity electricity) {
        final String route = String.format("%s/v1/%s/results/electricity", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(electricity)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/electricity/:id
    public CompletionStage<Optional<Electricity>> getElectricityResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/electricity/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Electricity.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (gas)
    //    POST          /v1/:orga/results/gas
    public CompletionStage<Optional<String>> addGasResult(final Gas gas) {
        final String route = String.format("%s/v1/%s/results/gas", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(gas)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/gas/:id
    public CompletionStage<Optional<Gas>> getGasResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/gas/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Gas.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (dpe)
    //    POST          /v1/:orga/results/dpe
    public CompletionStage<Optional<String>> addDpeResult(final DPE dpe) {
        final String route = String.format("%s/v1/%s/results/dpe", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(dpe)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/dpe/:id
    public CompletionStage<Optional<DPE>> getDpeResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/dpe/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), DPE.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (lead)
    //    POST          /v1/:orga/results/lead
    public CompletionStage<Optional<String>> addLeadResult(final Lead lead) {
        final String route = String.format("%s/v1/%s/results/lead", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(lead)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/lead/:id
    public CompletionStage<Optional<Lead>> getLeadResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/lead/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Lead.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (termite)
    //    POST          /v1/:orga/results/termite
    public CompletionStage<Optional<String>> addTermiteResult(final Termite termite) {
        final String route = String.format("%s/v1/%s/results/termite", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(termite)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/termite/:id
    public CompletionStage<Optional<Termite>> getTermiteResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/termite/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Termite.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // Add presta result (measurement)
    //    POST          /v1/:orga/results/measurement
    public CompletionStage<Optional<String>> addMeasurementResult(final Measurement measurement) {
        final String route = String.format("%s/v1/%s/results/measurement", baseRoute, tenant);
        return wsClient.url(route).post(Json.toJson(measurement)).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson().findValue("uuid").asText());
            } else {
                return Optional.empty();
            }
        });
    }

    // get presta result by result id
    //    GET           /v1/:orga/results/measurement/:id
    public CompletionStage<Optional<Measurement>> getMeasurementResult(String resultId) {
        final String route = String.format("%s/v1/%s/results/measurement/%s", baseRoute, tenant, resultId);
        return wsClient.url(route).get().thenApply((WSResponse wsResponse) -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), Measurement.class));
            } else {
                return Optional.empty();
            }
        });
    }

    public CompletionStage<List<PrestationWithEstate>> getPrestationWithEstateFromOrder(final String organization, final String order) {
        final String route = String.format("%s/v1/%s/prestationswithestate", baseRoute, organization);
        final List<Tuple2<String, String>> headers = new ArrayList<>();
        headers.add(new Tuple2<>("order", order));
        final WSRequest request = wsClient.url(route);
        headers.forEach(h -> request.addQueryParameter(h._1, h._2));
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<List<PrestationWithEstate>>() {
                });
            } else {
                return new ArrayList<>();
            }
        });
    }

    public CompletionStage<List<MaterializedIntervention>> getInterventionsFromOrder(final String organization, final String order, final String[] statusArray) {
        final String route = String.format("%s/v1/%s/orders/%s/interventions", baseRoute, organization, order);
        final WSRequest request = wsClient.url(route);
        Arrays.stream(statusArray).forEach(status -> {
            request.addQueryParameter("status", status);
        });
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                JsonNode nodes = wsResponse.asJson();
                if (nodes.isArray()) {
                    return StreamSupport.stream(nodes.spliterator(), true)
                            .map(InterventionUtils::buildIntervention)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                } else {
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
        });
//        }
    }

    //    PATCH          /v1/:orga/interventions/:id/update
    public CompletionStage<Boolean> updateIntervention(final String interventionId, InterventionStatus status, DateTime interventionDate, Optional<DateTime> closureDateOpt, String reportId, Optional<String> expertLabel) {
        final String route = String.format("%s/v1/%s/interventions/%s/update", baseRoute, tenant, interventionId);
        ObjectNode body = new ObjectMapper().createObjectNode()
                .put("status", status.index)
                .put("interventionDate", interventionDate.getMillis())
                .put("reportId", reportId);
        closureDateOpt.ifPresent(closureDate -> body.put("closureDate", closureDate.getMillis()));
        expertLabel.ifPresent(label -> body.put("expertLabel", label));

        return wsClient.url(route).patch(body).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return true;
            } else {
                logger.error("could not update status as expected");
                return false;
            }
        });
    }

    // GET      /v1/:orga/interventions/name/:name      api.v1.controllers.InterventionController.get(orga, id)
    public CompletionStage<Optional<MaterializedIntervention>> getInterventionFromName(final String organization, final String name) {
        final String route = String.format("%s/v1/%s/interventions/name/%s", baseRoute, organization, name);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return InterventionUtils.buildIntervention(wsResponse.asJson());
            } else {
                return Optional.empty();
            }
        });
    }

    // GET      /v1/:orga/interventions/:id      api.v1.controllers.InterventionController.get(orga, id)
    public CompletionStage<Optional<MaterializedIntervention>> getIntervention(final String organization, final String id) {
        final String route = String.format("%s/v1/%s/interventions/%s", baseRoute, organization, id);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return InterventionUtils.buildIntervention(wsResponse.asJson());
            } else {
                return Optional.empty();
            }
        });
    }

    // GET      /v1/:orga/prestations/:id/result      api.v1.controllers.PrestationsController.getResult(orga, prestaId)
    public CompletionStage<Optional<JsonNode>> getResult(final String organization, final String id) {
        final String route = String.format("%s/v1/%s/prestations/%s/result", baseRoute, organization, id);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.of(wsResponse.asJson());
            } else {
                return Optional.empty();
            }
        });
    }

    // PATCH       /v1/:orga/interventions/:id/bill          api.v1.controllers.InterventionController.patchBill(request: Request, orga, id)
    public CompletionStage<Boolean> patchBill(final String organization, final String id, final String bill) {
        final String route = String.format("%s/v1/%s/interventions/%s/bill", baseRoute, organization, id);
        final WSRequest request = wsClient.url(route);
        ObjectNode body = new ObjectMapper().createObjectNode()
                .put("bill", bill);
        return request.patch(body).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return true;
            } else {
                return false;
            }
        });
    }

    // POST       /v1/:orga/interventions/bill          api.v1.controllers.InterventionController.billInterventions(request: Request, orga)
    public CompletionStage<Boolean> billInterventions(final String organization, final String order, final List<String> interventions) {
        final String route = String.format("%s/v1/%s/interventions/bill", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        ObjectNode body = new ObjectMapper().createObjectNode()
                .put("order", order);
        ArrayNode arrayNode = body.putArray("interventions");
        for (String intervention : interventions) {
            arrayNode.add(intervention);
        }
        return request.post(body).thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return true;
            } else {
                return false;
            }
        });
    }

    // PATCH     /v1/:orga/prestations/billlines         api.v1.controllers.PrestationsController.patchBillLines(request: Request, orga)
    public CompletionStage<Boolean> patchBillLines(final String organization, final List<PrestationBillLine> prestationBillLines) {
        final String route = String.format("%s/v1/%s/prestations/billlines", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        try {
            String body = new ObjectMapper().writeValueAsString(prestationBillLines);
            return request.patch(body).thenApply(wsResponse -> {
                if (wsResponse.getStatus() < 300) {
                    return true;
                } else {
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error during patch bill lines in mission client");
            return CompletableFuture.completedFuture(false);
        }
    }

    // GET      /v1/:orga/interventions/name/:name      api.v1.controllers.InterventionController.get(orga, id)
    public CompletionStage<List<MaterializedIntervention>> getInterventionsFromBill(final String organization, final String bill) {
        final String route = String.format("%s/v1/%s/interventions", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        request.addQueryParameter("bill", bill);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                JsonNode nodes = wsResponse.asJson();
                if (nodes.isArray()) {
                    return StreamSupport.stream(nodes.spliterator(), true)
                            .map(InterventionUtils::buildIntervention)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
                } else {
                    return new ArrayList<>();
                }
            } else {
                return new ArrayList<>();
            }
        });
    }

    // PATCH         /v1/:orga/prestations                     api.v1.controllers.PrestationsController.set(request: Request, orga)
    public CompletionStage<Boolean> patchPrestations(final String organization, final SetPrestations prestations) {
        final String route = String.format("%s/v1/%s/prestations", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        try {
            return request.patch(Json.toJson(prestations)).thenApply(wsResponse -> {
                if (wsResponse.getStatus() < 300) {
                    return true;
                } else {
                    return false;
                }
            });
        } catch (Exception e) {
            logger.error("Error during patch bill lines in mission client");
            return CompletableFuture.completedFuture(false);
        }
    }

    //    GET           /v1/:orga/analyse-types                                 api.v1.controllers.AnalyseTypeController.getAll(orga)
    public CompletionStage<List<AnalyseType>> getAllAnalyseType(final String organization) {
        final String route = String.format("%s/v1/%s/analyse-types", baseRoute, organization);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return new ObjectMapper().convertValue(wsResponse.asJson(), new TypeReference<List<AnalyseType>>() {
                });
            } else {
                return new ArrayList<>();
            }
        });
    }

    //    GET           /v1/:orga/analyse-types/:uuid                           api.v1.controllers.AnalyseTypeController.get(orga, uuid)
    public CompletionStage<Optional<AnalyseType>> getAnalyseType(final String organization, final String analyseTypeId) {
        final String route = String.format("%s/v1/%s/analyse-types/%s", baseRoute, organization, analyseTypeId);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                return Optional.ofNullable(new ObjectMapper().convertValue(wsResponse.asJson(), AnalyseType.class));
            } else {
                return Optional.empty();
            }
        });
    }

    // GET      /v1/:orga/orders/estate/:estateId      api.v1.controllers.PrestationsController.get(orga, estateId)
    public CompletionStage<List<String>> getOrdersFromEstate(final String organization, final String estateId) {
        final String route = String.format("%s/v1/%s/orders/estate/%s", baseRoute, organization, estateId);
        final WSRequest request = wsClient.url(route);
        return request.get().thenApply(wsResponse -> {
            if (wsResponse.getStatus() < 300) {
                JsonNode nodes = wsResponse.asJson();
                if (nodes.isArray()) {
                    return new ObjectMapper().convertValue(wsResponse.asJson(), ArrayList.class);
                } else {
                    return new ArrayList<>();
                }
            } else {
                logger.error(wsResponse.getBody());
                return new ArrayList<>();
            }
        });
    }
}
