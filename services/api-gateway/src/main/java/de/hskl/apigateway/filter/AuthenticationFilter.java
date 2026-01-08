package de.hskl.apigateway.filter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${FIREBASE_EMULATOR_ENABLED}")
    private boolean emulatorEnabled;

    private static Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);

    // Routen ohne Auth
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/shipment/track",
            "/actuator/"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        if (isPublicPath(path)) {
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.warn("Missing or invalid Authorization header for path {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            FirebaseToken decodedToken;
            if (emulatorEnabled) {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(token, false);
            } else {
                decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            }

            String userId = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // read Claims
            Object roleClaim = decodedToken.getClaims().get("role");
            String role = roleClaim != null ? roleClaim.toString() : "CUSTOMER";

            // pass on Role & UserId
            ServerWebExchange mutatedExchange = exchange.mutate()
                    .request(r -> r
                            .header("X-Auth-User-Id", userId)
                            .header("X-Auth-User-Role", role))
                    .build();

            logger.debug("Authentication request: user={}, role={}", userId, role);
            return chain.filter(mutatedExchange);
        } catch (FirebaseAuthException e) {
            logger.warn("Token verification failed: {}", e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
