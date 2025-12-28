package de.hskl.trackingservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEvent {
    private UUID shipmentId;
    private String status;
    private String message;
    private Double lat;
    private Double lng;
    private Instant timestamp;
}
