package de.hskl.trackingservice.service;

import de.hskl.trackingservice.dto.TrackingUpdateDto;
import de.hskl.trackingservice.entity.TrackingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TrackingService {
    private final PubSubPublisherService pubSubPublisher;

    public void processTrackingUpdate(TrackingUpdateDto dto) {
        log.info("Processing tracking update for shipment: {}", dto.shipmentId());

        if (dto.shipmentId() == null) {
            throw new IllegalArgumentException("Shipment ID is required");
        }

        TrackingEvent event = TrackingEvent.builder()
                .shipmentId(dto.shipmentId())
                .status(dto.status())
                .message(dto.message())
                .lat(dto.lat())
                .lng(dto.lng())
                .timestamp(dto.timestamp() != null ? dto.timestamp() : Instant.now())
                .build();

        pubSubPublisher.publishTrackingUpdate(event);

        log.info("Successfully processed tracking update for shipment: {}", dto.shipmentId());
    }
}
