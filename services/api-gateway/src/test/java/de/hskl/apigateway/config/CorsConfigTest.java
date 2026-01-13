package de.hskl.apigateway.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.cors.reactive.CorsWebFilter;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
public class CorsConfigTest {
    @Autowired
    private CorsWebFilter corsWebFilter;

    @Test
    void shouldCreateCorsFilter() {
        assertThat(corsWebFilter).isNotNull();
    }
}
