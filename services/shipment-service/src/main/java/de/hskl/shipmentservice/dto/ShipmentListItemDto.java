package de.hskl.shipmentservice.dto;

import de.hskl.shipmentservice.entity.Shipment;

import java.time.Instant;
import java.util.UUID;

public record ShipmentListItemDto(
        UUID id,
        String sender,
        String receiver,
        String currentStatus,
        Instant expectedDelivery
) {
    public static ShipmentListItemDto from(Shipment s) {
        return new ShipmentListItemDto(
                s.getId(),
                s.getSender(),
                s.getReceiver(),
                s.getCurrentStatus(),
                s.getExpectedDelivery()
        );
    }
}
