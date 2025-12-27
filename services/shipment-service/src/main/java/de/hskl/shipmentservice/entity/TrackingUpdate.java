package de.hskl.shipmentservice.entity;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
public class TrackingUpdate {
    private UUID shipmentId;
    private String status;
    private String message;
    private Double lat;
    private Double lng;
    private Instant timestamp;
}
