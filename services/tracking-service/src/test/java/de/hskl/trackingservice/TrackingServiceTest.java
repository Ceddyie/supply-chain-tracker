package de.hskl.trackingservice;

import de.hskl.trackingservice.dto.TrackingUpdateDto;
import de.hskl.trackingservice.service.PubSubPublisherService;
import de.hskl.trackingservice.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TrackingServiceTest {
    @Mock
    private PubSubPublisherService pubSubPublisher;
    @InjectMocks
    private TrackingService trackingService;

    @Test
    void processTrackingUpdate_shouldPublishEvent() {
        TrackingUpdateDto dto = new TrackingUpdateDto(
                UUID.randomUUID(),
                "IN_TRANSIT",
                "Package scanned at warehouse",
                50.1234,
                8.5678,
                Instant.now()
        );

        doNothing().when(pubSubPublisher).publishTrackingUpdate(any());

        trackingService.processTrackingUpdate(dto);

        verify(pubSubPublisher, times(1)).publishTrackingUpdate(any());
    }

    @Test
    void processTrackingUpdate_withNullShipmentId_shouldThrowException() {
        TrackingUpdateDto dto = new TrackingUpdateDto(
                null,
                "IN_TRANSIT",
                "Package scanned at warehouse",
                50.1234,
                8.5678,
                Instant.now()
        );

        assertThrows(IllegalArgumentException.class, () -> trackingService.processTrackingUpdate(dto));
    }
}
