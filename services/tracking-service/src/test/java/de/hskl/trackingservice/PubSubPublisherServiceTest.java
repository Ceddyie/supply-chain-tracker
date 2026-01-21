package de.hskl.trackingservice;

import com.google.cloud.spring.pubsub.core.PubSubTemplate;
import de.hskl.trackingservice.entity.TrackingEvent;
import de.hskl.trackingservice.service.PubSubPublisherService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PubSubPublisherServiceTest {
    @Mock
    private PubSubTemplate pubSubTemplate;

    private PubSubPublisherService publisherService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        publisherService = new PubSubPublisherService(pubSubTemplate, objectMapper);
        ReflectionTestUtils.setField(publisherService, "topicName", "test-topic");
    }

    @Test
    void publishTrackingUpdate_shouldPublishToCorrectTopic() {
        TrackingEvent trackingEvent = TrackingEvent.builder()
                .shipmentId(UUID.randomUUID())
                .status("IN_TRANSIT")
                .message("Test message")
                .lat(50.0)
                .lng(8.0)
                .timestamp(Instant.now())
                .build();

        // Return a completed future to simulate successful publish
        CompletableFuture<String> future = CompletableFuture.completedFuture("message-id");
        when(pubSubTemplate.publish(anyString(), anyString())).thenReturn(future);

        publisherService.publishTrackingUpdate(trackingEvent);

        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> messageCaptor = ArgumentCaptor.forClass(String.class);

        verify(pubSubTemplate, times(1)).publish(topicCaptor.capture(), messageCaptor.capture());

        assertEquals("test-topic", topicCaptor.getValue());
        assertTrue(messageCaptor.getValue().contains(trackingEvent.getShipmentId().toString()));
    }

    @Test
    void publishTrackingUpdate_shouldThrowOnFailure() {
        TrackingEvent trackingEvent = TrackingEvent.builder()
                .shipmentId(UUID.randomUUID())
                .status("IN_TRANSIT")
                .message("Test message")
                .lat(50.0)
                .lng(8.0)
                .timestamp(Instant.now())
                .build();

        // Return a failed future to simulate publish failure
        CompletableFuture<String> failedFuture = new CompletableFuture<>();
        failedFuture.completeExceptionally(new RuntimeException("Pub/Sub error"));
        when(pubSubTemplate.publish(anyString(), anyString())).thenReturn(failedFuture);

        assertThrows(RuntimeException.class, () -> publisherService.publishTrackingUpdate(trackingEvent));
    }
}
