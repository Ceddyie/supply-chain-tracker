package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Checkpoint;

import java.time.Instant;

public record CheckpointTrackingDto(
        Instant timestamp,
        String status,
        String message,
        Double lat,
        Double lng
) {
    public static CheckpointTrackingDto from(Checkpoint checkpoint) {
        return new CheckpointTrackingDto(
                checkpoint.getTimestamp(),
                checkpoint.getStatus(),
                checkpoint.getMessage(),
                checkpoint.getLat(),
                checkpoint.getLng()
        );
    }
}
