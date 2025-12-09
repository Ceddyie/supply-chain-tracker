package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Checkpoint;

import java.time.Instant;

public record CheckpointDto(
        Instant timestamp,
        String status,
        String message,
        Double lat,
        Double lng
) {
    public static CheckpointDto from(Checkpoint checkpoint) {
        return new CheckpointDto(
                checkpoint.getTimestamp(),
                checkpoint.getStatus(),
                checkpoint.getMessage(),
                checkpoint.getLat(),
                checkpoint.getLng()
        );
    }
}
