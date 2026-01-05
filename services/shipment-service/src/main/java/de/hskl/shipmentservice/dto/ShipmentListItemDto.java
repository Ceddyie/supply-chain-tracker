package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Shipment;

import java.time.Instant;
import java.util.UUID;

public record ShipmentListItemDto(
        UUID id,
        String trackingId,
        String sender,
        String receiver,
        String currentStatus,
        Instant expectedDelivery,
        Instant createdAt
) {
    public static ShipmentListItemDto from(Shipment s) {
        return new ShipmentListItemDto(
                s.getId(),
                s.getTrackingId(),
                s.getSender(),
                s.getReceiver(),
                s.getCurrentStatus(),
                s.getExpectedDelivery(),
                s.getCreatedAt()
        );
    }
}
