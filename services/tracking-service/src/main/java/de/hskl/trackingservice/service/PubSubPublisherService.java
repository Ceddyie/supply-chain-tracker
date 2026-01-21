package de.hskl.trackingservice.service;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import de.hskl.trackingservice.entity.TrackingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
            pubSubTemplate.publish(topicName, json).get(30, TimeUnit.SECONDS);
            log.info("Published tracking update for shipment: {}", event.getShipmentId());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Publishing interrupted for shipment: {}", event.getShipmentId(), e);
            throw new RuntimeException("Publishing interrupted", e);
        } catch (ExecutionException e) {
            log.error("Failed to publish tracking update for shipment: {}", event.getShipmentId(), e);
            throw new RuntimeException("Failed to publish message", e);
        } catch (TimeoutException e) {
            log.error("Timeout publishing tracking update for shipment: {}", event.getShipmentId(), e);
            throw new RuntimeException("Publishing timed out", e);
        } catch (Exception e) {
            log.error("Error serializing tracking event", e);
            throw new RuntimeException("Serialization error", e);
        }
    }
}
