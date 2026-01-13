package de.hskl.apigateway.integration;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.io.IOException;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class GatewayIntegrationTest {
    @LocalServerPort
    private int port;

    @Autowired
    private WebTestClient webTestClient;

    private static MockWebServer shipmentServiceMock;
    private static MockWebServer trackingServiceMock;

    @BeforeAll
    static void setUpMockServers() throws IOException {
        shipmentServiceMock = new MockWebServer();
        shipmentServiceMock.start();

        trackingServiceMock = new MockWebServer();
        trackingServiceMock.start();
    }

    @AfterAll
    static void tearDownMockServers() throws IOException {
        shipmentServiceMock.shutdown();
        trackingServiceMock.shutdown();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("SHIPMENT_SERVICE_URL", () ->
                String.format("http://localhost:%s", shipmentServiceMock.getPort()));
        registry.add("TRACKING_SERVICE_URL", () ->
                String.format("http://localhost:%s", trackingServiceMock.getPort()));
        registry.add("FIREBASE_EMULATOR_ENABLED", () -> "true");
    }

    @Test
    void shouldReturnUnauthorizedWithoutToken() {
        webTestClient.get()
                .uri("/api/shipment")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldAllowPublicTrackingEndpoint() {
        shipmentServiceMock.enqueue(new MockResponse()
                .setBody("{\"id\": \"123\", \"status\": \"IN_TRANSIT\"}")
                .addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));

        webTestClient.get()
                .uri("/api/shipment/track/ABC123")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldAllowActuatorHealth() {
        webTestClient.get()
                .uri("/actuator/health")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void shouldRejectInvalidBearerToken() {
        webTestClient.get()
                .uri("/api/shipment")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldHandleCorsPreflightRequest() {
        webTestClient.options()
                .uri("/api/shipment")
                .header(HttpHeaders.ORIGIN, "http://localhost:5173")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectHeader().exists(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }

    @Test
    void shouldRejectCorsFromUnknownOrigin() {
        webTestClient.options()
                .uri("/api/shipment")
                .header(HttpHeaders.ORIGIN, "http://malicious-site.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                .exchange()
                .expectHeader().doesNotExist(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN);
    }
}
