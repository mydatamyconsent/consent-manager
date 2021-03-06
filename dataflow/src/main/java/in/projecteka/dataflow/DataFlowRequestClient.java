package in.projecteka.dataflow;

import in.projecteka.dataflow.model.DataFlowRequestResult;
import in.projecteka.dataflow.properties.GatewayServiceProperties;
import in.projecteka.library.clients.model.ClientError;
import in.projecteka.library.common.ServiceAuthentication;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static in.projecteka.dataflow.Constants.HDR_HIU_ID;
import static in.projecteka.dataflow.Constants.PATH_DATA_FLOW_CM_ON_REQUEST;
import static in.projecteka.library.common.Constants.CORRELATION_ID;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

public class DataFlowRequestClient {

    private final WebClient webClient;
    private final GatewayServiceProperties gatewayServiceProperties;
    private final ServiceAuthentication serviceAuthentication;

    public DataFlowRequestClient(WebClient.Builder webClient,
                                 GatewayServiceProperties gatewayServiceProperties,
                                 ServiceAuthentication serviceAuthentication) {
        this.webClient = webClient.build();
        this.gatewayServiceProperties = gatewayServiceProperties;
        this.serviceAuthentication = serviceAuthentication;
    }

    public Mono<Void> sendHealthInformationResponseToGateway(DataFlowRequestResult dataFlowRequest, String hiuId) {
        return serviceAuthentication.authenticate()
                .flatMap(authToken ->
                        webClient
                                .post()
                                .uri(gatewayServiceProperties.getBaseUrl() + PATH_DATA_FLOW_CM_ON_REQUEST)
                                .contentType(MediaType.APPLICATION_JSON)
                                .header(AUTHORIZATION, authToken)
                                .header(HDR_HIU_ID, hiuId)
                                .header(CORRELATION_ID, MDC.get(CORRELATION_ID))
                                .bodyValue(dataFlowRequest)
                                .retrieve()
                                .onStatus(httpStatus -> httpStatus.value() == 400,
                                        clientResponse -> Mono.error(ClientError.invalidResponseFromGateway()))
                                .onStatus(httpStatus -> httpStatus.value() == 401,
                                        clientResponse -> Mono.error(ClientError.unAuthorized()))
                                .onStatus(HttpStatus::is5xxServerError,
                                        clientResponse -> Mono.error(ClientError.networkServiceCallFailed()))
                                .toBodilessEntity()
                                .timeout(Duration.ofMillis(gatewayServiceProperties.getRequestTimeout())))
                .then();
    }
}
