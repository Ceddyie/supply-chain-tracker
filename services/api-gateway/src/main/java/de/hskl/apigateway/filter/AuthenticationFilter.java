package de.hskl.apigateway.filter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

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

        // Mock Auth
        String userRole = exchange.getRequest().getHeaders().getFirst("X-User-Role");
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");

        if (userRole == null || userId == null) {
            logger.warn("Missing auth headers for path: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        // Rolle und User Id weitergeben
        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(r -> r
                        .header("X-Auth-User-Id", userId)
                        .header("X-Auth-User-Role", userRole))
                .build();

        logger.debug("Authentication request: user={}, role={}", userId, userRole);

        return chain.filter(mutatedExchange);
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    @Override
    public int getOrder() {
        return 0;
    }
}
