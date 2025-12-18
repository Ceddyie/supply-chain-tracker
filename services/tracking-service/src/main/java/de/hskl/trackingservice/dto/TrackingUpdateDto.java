package de.hskl.trackingservice.dto;

import java.time.Instant;
import java.util.UUID;

public record TrackingUpdateDto(
        UUID shipmentId,
        String status,
        String message,
        Double lat,
        Double lng,
        Instant timestamp
) {
}
