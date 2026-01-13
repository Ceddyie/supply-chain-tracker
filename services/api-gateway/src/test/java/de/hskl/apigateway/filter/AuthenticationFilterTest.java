package de.hskl.apigateway.filter;

import com.google.firebase.ErrorCode;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationFilterTest {
    private AuthenticationFilter authenticationFilter;

    @Mock
    private GatewayFilterChain filterChain;
    @Mock
    private FirebaseAuth firebaseAuth;
    @Mock
    private FirebaseToken firebaseToken;

    @BeforeEach
    void setUp() {
        authenticationFilter = new AuthenticationFilter();
        ReflectionTestUtils.setField(authenticationFilter, "emulatorEnabled", true);
    }

    @Test
    void shouldAllowPublicPathsWithoutAuth() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment/track/ABC123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldAllowActuatorEndpoints() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/actuator/health").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(filterChain.filter(any())).thenReturn(Mono.empty());

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

        verify(filterChain).filter(exchange);
    }

    @Test
    void shouldRejectRequestWithoutAuthHeader() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldRejectRequestWithInvalidAuthHeaderFormat() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "InvalidFormat token123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldRejectRequestWithOnlyBearerKeyword() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        verify(filterChain, never()).filter(any());
    }

    @Test
    void shouldAuthenticateValidToken() throws FirebaseAuthException {
        MockServerHttpRequest request = MockServerHttpRequest.get("(api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer token123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(firebaseToken.getUid()).thenReturn("user-123");
        when(firebaseToken.getEmail()).thenReturn("test@example.com");
        when(firebaseToken.getClaims()).thenReturn(Map.of("role", "SENDER"));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = Mockito.mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(eq("token123"), eq(false))).thenReturn(firebaseToken);
            when(filterChain.filter(any())).thenReturn(Mono.empty());

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

            verify(filterChain).filter(any());
        }
    }

    @Test
    void shouldRejectInvalidToken() throws FirebaseAuthException {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = Mockito.mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(anyString(), anyBoolean())).thenAnswer(invocation -> {
                throw mock(FirebaseAuthException.class);
            });

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
            verify(filterChain, never()).filter(any());
        }
    }

    @Test
    void shouldDefaultToCustomerRoleWhenNoRoleClaim() throws FirebaseAuthException {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer token123").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(firebaseToken.getUid()).thenReturn("user-123");
        when(firebaseToken.getEmail()).thenReturn("test@example.com");
        when(firebaseToken.getClaims()).thenReturn(Map.of());

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = Mockito.mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(eq("token123"), eq(false))).thenReturn(firebaseToken);
            when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
                ServerWebExchange mutatedExchange = invocation.getArgument(0);
                String role = mutatedExchange.getRequest().getHeaders().getFirst("X-Auth-User-Role");
                assertThat(role).isEqualTo("CUSTOMER");
                return Mono.empty();
            });

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();
        }
    }

    @Test
    void shouldPassUserIdAndRoleHeaders() throws FirebaseAuthException {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer valid-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(firebaseToken.getUid()).thenReturn("user-456");
        when(firebaseToken.getEmail()).thenReturn("sender@example.com");
        when(firebaseToken.getClaims()).thenReturn(Map.of("role", "STATION"));

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(eq("valid-token"), eq(false))).thenReturn(firebaseToken);

            when(filterChain.filter(any(ServerWebExchange.class))).thenAnswer(invocation -> {
                ServerWebExchange mutatedExchange = invocation.getArgument(0);
                HttpHeaders headers = mutatedExchange.getRequest().getHeaders();

                assertThat(headers.getFirst("X-Auth-User-Id")).isEqualTo("user-456");
                assertThat(headers.getFirst("X-Auth-User-Role")).isEqualTo("STATION");
                return Mono.empty();
            });

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

            verify(filterChain).filter(any(ServerWebExchange.class));
        }
    }

    @Test
    void shouldUseCheckRevokedFalseInEmulatorMode() throws FirebaseAuthException {
        ReflectionTestUtils.setField(authenticationFilter, "emulatorEnabled", true);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer test-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(firebaseToken.getUid()).thenReturn("user-123");
        when(firebaseToken.getEmail()).thenReturn("test@example.com");
        when(firebaseToken.getClaims()).thenReturn(Map.of());

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(eq("test-token"), eq(false))).thenReturn(firebaseToken);
            when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain))
                    .verifyComplete();

            verify(firebaseAuth).verifyIdToken("test-token", false);
        }
    }

    @Test
    void shouldUseCheckRevokedTrueInProductionMode() throws FirebaseAuthException {
        ReflectionTestUtils.setField(authenticationFilter, "emulatorEnabled", false);

        MockServerHttpRequest request = MockServerHttpRequest.get("/api/shipment").header(HttpHeaders.AUTHORIZATION, "Bearer test-token").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);

        when(firebaseToken.getUid()).thenReturn("user-123");
        when(firebaseToken.getEmail()).thenReturn("test@example.com");
        when(firebaseToken.getClaims()).thenReturn(Map.of());

        try (MockedStatic<FirebaseAuth> firebaseAuthMock = mockStatic(FirebaseAuth.class)) {
            firebaseAuthMock.when(FirebaseAuth::getInstance).thenReturn(firebaseAuth);
            when(firebaseAuth.verifyIdToken(eq("test-token"))).thenReturn(firebaseToken);
            when(filterChain.filter(any(ServerWebExchange.class))).thenReturn(Mono.empty());

            StepVerifier.create(authenticationFilter.filter(exchange, filterChain)).verifyComplete();

            verify(firebaseAuth).verifyIdToken("test-token");
        }
    }

    @Test
    void shouldHaveCorrectFilterOrder() {
        assertThat(authenticationFilter.getOrder()).isEqualTo(0);
    }

    @Test
    void shouldRunAfterLoggingFilter() {
        LoggingFilter loggingFilter = new LoggingFilter();
        assertThat(authenticationFilter.getOrder()).isGreaterThan(loggingFilter.getOrder());
    }
}
