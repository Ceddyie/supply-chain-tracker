package de.hskl.shipmentservice.PubSub;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import de.hskl.shipmentservice.service.ShipmentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Component
@Slf4j
@Profile("!prod")
public class PubSubSubscriber {
    @Value("${pubsub.subscription.tracking-updates}")
    private String subscription;

    @Autowired
    private ShipmentService shipmentService;

    @Autowired
    private PubSubTemplate pubSubTemplate;

    @PostConstruct
    public void subscribe() {
        pubSubTemplate.subscribe(subscription, message -> {
            String payload = message.getPubsubMessage().getData().toStringUtf8();

            log.info("Received message: {}", payload);
            try {
                shipmentService.updateShipment(payload);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

            message.ack();
        });
    }
}
