package de.hskl.shipmentservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hskl.shipmentservice.dto.*;
import de.hskl.shipmentservice.entity.Checkpoint;
import de.hskl.shipmentservice.entity.Shipment;
import de.hskl.shipmentservice.entity.TrackingUpdate;
import de.hskl.shipmentservice.exceptions.GlobalExceptionHandler;
import de.hskl.shipmentservice.repository.CheckpointRepository;
import de.hskl.shipmentservice.repository.ShipmentRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final CheckpointRepository checkpointRepository;

    private static final String CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final SecureRandom random = new SecureRandom();

    @Transactional
    public ShipmentDetailDto createShipment(CreateShipmentDto dto, String ownerUserId) {
        var now = Instant.now();
        String trackingId = generateTrackingId();

        Shipment shipment = Shipment.builder()
                .trackingId(trackingId)
                .ownerUserId(ownerUserId)
                .sender(dto.sender())
                .receiver(dto.receiver())
                .receiverStreet(dto.receiverStreet())
                .receiverCity(dto.receiverCity())
                .currentStatus("CREATED")
                .expectedDelivery(dto.expectedDelivery())
                .createdAt(now)
                .updatedAt(now)
                .build();

        shipmentRepository.save(shipment);

        Checkpoint checkpoint = Checkpoint.builder()
                .shipment(shipment)
                .timestamp(now)
                .status("CREATED")
                .message("Shipment created")
                .build();

        checkpointRepository.save(checkpoint);

        return mapDetail(shipment, List.of(checkpoint));
    }

    private String generateTrackingId() {
        StringBuilder sb = new StringBuilder("PKG-");
        for (int i = 0; i < 8; i++) {
            sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
        }
        return sb.toString();
    }

    @Transactional(readOnly = true)
    public ShipmentDetailDto getShipment(UUID id, String requesterUserId, boolean isAdmin) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ShipmentNotFoundException(id));

        if (!isAdmin &&
                !requesterUserId.equals(shipment.getOwnerUserId())) {
            throw new GlobalExceptionHandler.AccessDeniedException("Forbidden");
        }
        var checkpoints = checkpointRepository.findByShipmentIdOrderByTimestampAsc(id);
        return mapDetail(shipment, checkpoints);
    }

    @Transactional(readOnly = true)
    public List<ShipmentListItemDto> listForUser(String ownerUserId) {
        List<Shipment> shipments = shipmentRepository.findByOwnerUserId(ownerUserId);
        return shipments.stream().map(ShipmentListItemDto::from).toList();
    }

    private ShipmentDetailDto mapDetail(Shipment shipment, List<Checkpoint> checkpoints) {
        var timeline = checkpoints.stream()
                .map(CheckpointDto::from).toList();

        return ShipmentDetailDto.from(shipment, timeline);
    }

    public void updateShipment(String payload) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        TrackingUpdate trackingUpdate = mapper.readValue(payload, TrackingUpdate.class);

        log.info("Tracking Update: {}", trackingUpdate);

        Shipment shipment = shipmentRepository.findById(trackingUpdate.getShipmentId())
                .orElseThrow(() -> new EntityNotFoundException("Shipment not found: " + trackingUpdate.getShipmentId()));

        shipment.setCurrentStatus(trackingUpdate.getStatus());
        shipment.setLastLat(trackingUpdate.getLat());
        shipment.setLastLng(trackingUpdate.getLng());
        shipment.setUpdatedAt(Instant.now());
        shipmentRepository.save(shipment);

        Checkpoint checkpoint = Checkpoint.builder()
                .shipment(shipment)
                .timestamp(trackingUpdate.getTimestamp() != null ? trackingUpdate.getTimestamp() : Instant.now())
                .status(trackingUpdate.getStatus())
                .message(trackingUpdate.getMessage())
                .lat(trackingUpdate.getLat())
                .lng(trackingUpdate.getLng())
                .build();

        log.info("Checkpoint {}", checkpoint);
        checkpointRepository.save(checkpoint);
    }

    public ShipmentTrackingDto getTrackingInfo(String trackingId) {
        Shipment shipment = shipmentRepository.findByTrackingId(trackingId);
        if (shipment == null) {
            throw new GlobalExceptionHandler.TrackingNotFoundException(trackingId);
        }
        var checkpoints = checkpointRepository.findByShipmentIdOrderByTimestampAsc(shipment.getId());
        return trackingMap(shipment, checkpoints);
    }

    private ShipmentTrackingDto trackingMap(Shipment shipment, List<Checkpoint> checkpoints) {
        var timeline = checkpoints.stream().map(CheckpointTrackingDto::from).toList();
        return ShipmentTrackingDto.from(shipment, timeline);
    }
}
