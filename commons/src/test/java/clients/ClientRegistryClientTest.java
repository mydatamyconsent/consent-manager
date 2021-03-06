package clients;

import in.projecteka.library.clients.ClientRegistryClient;
import in.projecteka.library.clients.model.Identifier;
import in.projecteka.library.clients.model.Provider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.IOException;

import static clients.TestBuilders.address;
import static clients.TestBuilders.coding;
import static clients.TestBuilders.identifier;
import static clients.TestBuilders.provider;
import static clients.TestBuilders.telecom;
import static clients.TestBuilders.type;
import static common.TestBuilders.OBJECT_MAPPER;
import static java.util.List.of;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class ClientRegistryClientTest {
    @Captor
    private ArgumentCaptor<ClientRequest> captor;
    private ClientRegistryClient clientRegistryClient;
    @Mock
    private ExchangeFunction exchangeFunction;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        WebClient.Builder webClientBuilder = WebClient.builder().exchangeFunction(exchangeFunction);
        clientRegistryClient = new ClientRegistryClient(webClientBuilder, "http://localhost:8000");
    }

    @Test
    void getProvidersByGivenName() throws IOException {
        var address = address().use("work").build();
        var telecommunication = telecom().use("work").build();
        var identifier = identifier().use(Identifier.IdentifierType.OFFICIAL.toString()).build();
        var source = provider()
                .addresses(of(address))
                .telecoms(of(telecommunication))
                .identifiers(of(identifier))
                .name("Max")
                .build();
        var providerJson = OBJECT_MAPPER.writeValueAsString(source);
        when(exchangeFunction.exchange(captor.capture()))
                .thenReturn(Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(providerJson)
                                .build()));

        StepVerifier.create(clientRegistryClient.providersOf("Max"))
                .assertNext(provider -> {
                    assertThat(provider.getName()).isEqualTo(source.getName());
                    assertThat(provider.getAddresses().get(0).getCity())
                            .isEqualTo(source.getAddresses().get(0).getCity());
                    assertThat(provider.getTelecoms().get(0).getValue())
                            .isEqualTo(source.getTelecoms().get(0).getValue());
                    assertThat(provider.getTypes().get(0).getCoding().get(0).getCode())
                            .isEqualTo(source.getTypes().get(0).getCoding().get(0).getCode());
                })
                .verifyComplete();

        assertThat(captor.getValue().url().toString()).isEqualTo("http://localhost:8000/api/2.0/organizations?name=Max");
    }

    @Test
    void getProviderById() throws IOException {
        var address = address().use("work").build();
        var telecommunication = telecom().use("work").build();
        var identifier = identifier().use(Identifier.IdentifierType.OFFICIAL.toString()).build();
        var source = provider()
                .addresses(of(address))
                .telecoms(of(telecommunication))
                .identifiers(of(identifier))
                .name("Max")
                .build();
        var providerJson = OBJECT_MAPPER.writeValueAsString(source);
        when(exchangeFunction.exchange(captor.capture()))
                .thenReturn(Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(providerJson)
                                .build()));

        StepVerifier.create(clientRegistryClient.providerWith("10000005"))
                .assertNext(provider -> {
                    assertThat(provider.getName()).isEqualTo(source.getName());
                    assertThat(provider.getAddresses().get(0).getCity())
                            .isEqualTo(source.getAddresses().get(0).getCity());
                    assertThat(provider.getTelecoms().get(0).getValue())
                            .isEqualTo(source.getTelecoms().get(0).getValue());
                    assertThat(provider.getTypes().get(0).getCoding().get(0).getCode())
                            .isEqualTo(source.getTypes().get(0).getCoding().get(0).getCode());
                })
                .verifyComplete();

        assertThat(captor.getValue().url().toString()).isEqualTo("http://localhost:8000/api/2.0/organizations/10000005");
    }

    @Test
    void getProviderByGivenId() throws IOException {
        Provider provider = provider()
                .name("Provider")
                .addresses(of(address().city("Provider City").build()))
                .telecoms(of(telecom().value("Telecom").build()))
                .types(of(type().coding(of(coding().code("Code").build())).build()))
                .build();

        String providerJsonResponse = OBJECT_MAPPER.writeValueAsString(provider);
        when(exchangeFunction.exchange(captor.capture()))
                .thenReturn(Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(providerJsonResponse)
                                .build()));

        StepVerifier.create(clientRegistryClient.providerWith("10000003"))
                .assertNext(providerResponse -> {
                    assertThat(providerResponse.getName()).isEqualTo(provider.getName());
                    assertThat(providerResponse.getAddresses().get(0).getCity())
                            .isEqualTo(provider.getAddresses().get(0).getCity());
                    assertThat(providerResponse.getTelecoms().get(0).getValue())
                            .isEqualTo(provider.getTelecoms().get(0).getValue());
                    assertThat(providerResponse.getTypes().get(0).getCoding().get(0).getCode())
                            .isEqualTo(provider.getTypes().get(0).getCoding().get(0).getCode());
                })
                .verifyComplete();

        assertThat(captor.getValue().url().toString()).isEqualTo("http://localhost:8000/api/2.0/organizations/10000003");
    }
}
