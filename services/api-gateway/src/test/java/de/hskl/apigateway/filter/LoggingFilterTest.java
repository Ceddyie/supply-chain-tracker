package de.hskl.apigateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class LoggingFilterTest {
    private LoggingFilter loggingFilter;

    @Mock
    private GatewayFilterChain filterChain;

    @BeforeEach
    void setUp() {
        loggingFilter = new LoggingFilter();
    }

    @Test
    void shouldLogIncomingRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment/123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldLogPostRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/shipment").body("{}");
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldContinueChainAfterLogging() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tracking/update").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(exchange)).thenReturn(Mono.empty());

        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain, times(1)).filter(exchange);
    }

    @Test
    void shouldHandleErrorsInChain() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        RuntimeException error = new RuntimeException("Downstream error");
        when(filterChain.filter(any())).thenReturn(Mono.error(error));

        StepVerifier.create(loggingFilter.filter(exchange, filterChain)).expectError(RuntimeException.class).verify();
    }

    @Test
    void shouldHaveHighestPriority() {
        assertThat(loggingFilter.getOrder()).isEqualTo(-1);
    }

    @Test
    void shouldRunBeforeAuthenticationFilter() {
        AuthenticationFilter authFilter = new AuthenticationFilter();
        assertThat(loggingFilter.getOrder()).isLessThan(authFilter.getOrder());
    }
}
