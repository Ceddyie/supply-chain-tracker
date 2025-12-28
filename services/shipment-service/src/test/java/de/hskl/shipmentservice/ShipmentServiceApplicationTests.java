package de.hskl.shipmentservice;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@ActiveProfiles("test")
class ShipmentServiceApplicationTests {
    @MockitoBean
    private PubSubTemplate pubSubTemplate;

    @Test
    void contextLoads() {
    }

}
