package de.hskl.shipmentservice.dto;


import java.time.Instant;

public record CreateShipmentDto(
        String sender,
        String receiver,
        Instant expectedDelivery
) {
}
