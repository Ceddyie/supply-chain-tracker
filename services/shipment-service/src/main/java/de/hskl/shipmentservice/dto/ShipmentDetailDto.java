package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Shipment;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ShipmentDetailDto(
        UUID id,
        String trackingId,
        String sender,
        String receiver,
        String currentStatus,
        Instant expectedDelivery,
        Double lastLat,
        Double lastLng,
        List<CheckpointDto> timeline
) {
    public static ShipmentDetailDto from(Shipment shipment, List<CheckpointDto> checkpoints) {
        return new ShipmentDetailDto(
                shipment.getId(),
                shipment.getTrackingId(),
                shipment.getSender(),
                shipment.getReceiver(),
                shipment.getCurrentStatus(),
                shipment.getExpectedDelivery(),
                shipment.getLastLat(),
                shipment.getLastLng(),
                checkpoints
        );
    }
}
