package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Shipment;

import java.time.Instant;
import java.util.List;

public record ShipmentTrackingDto(
        String trackingId,
        String status,
        Instant expectedDelivery,
        List<CheckpointTrackingDto> checkpoints
) {
    public static ShipmentTrackingDto from(Shipment shipment, List<CheckpointTrackingDto> checkpoints) {
        return new ShipmentTrackingDto(
                shipment.getTrackingId(),
                shipment.getCurrentStatus(),
                shipment.getExpectedDelivery(),
                checkpoints
        );
    }
}
