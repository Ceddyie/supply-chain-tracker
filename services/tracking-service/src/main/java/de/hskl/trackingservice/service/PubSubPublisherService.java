package de.hskl.trackingservice.service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import de.hskl.trackingservice.entity.TrackingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class PubSubPublisherService {
    private final PubSubTemplate pubSubTemplate;
    private final ObjectMapper objectMapper;

    @Value("${pubsub.topic.tracking-updates}")
    private String topicName;

    public void publishTrackingUpdate(TrackingEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            pubSubTemplate.publish(topicName, json);
            log.info("Published tracking update for shipment: {}", event.getShipmentId());
        } catch (Exception e) {
            log.error("Error serializing tracking event", e);
        }
    }
}
