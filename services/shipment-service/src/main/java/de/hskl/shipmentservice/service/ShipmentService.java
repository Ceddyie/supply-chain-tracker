package de.hskl.shipmentservice.service;

import de.hskl.shipmentservice.dto.CheckpointDto;
import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.dto.ShipmentListItemDto;
import de.hskl.shipmentservice.entity.Checkpoint;
import de.hskl.shipmentservice.entity.Shipment;
import de.hskl.shipmentservice.exceptions.GlobalExceptionHandler;
import de.hskl.shipmentservice.repository.CheckpointRepository;
import de.hskl.shipmentservice.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ShipmentService {
    private final ShipmentRepository shipmentRepository;
    private final CheckpointRepository checkpointRepository;

    @Transactional
    public ShipmentDetailDto createShipment(CreateShipmentDto dto, String ownerUserId, String companyId) {
        var now = Instant.now();
        Shipment shipment = Shipment.builder()
                .ownerUserId(ownerUserId)
                .companyId(companyId)
                .sender(dto.sender())
                .receiver(dto.receiver())
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

    @Transactional(readOnly = true)
    public ShipmentDetailDto getShipment(UUID id, String requesterUserId, String requesterCompanyId, boolean isAdmin) {
        Shipment shipment = shipmentRepository.findById(id)
                .orElseThrow(() -> new GlobalExceptionHandler.ShipmentNotFoundException(id));

        if (!isAdmin &&
                !requesterUserId.equals(shipment.getOwnerUserId()) &&
            !requesterCompanyId.equals(shipment.getCompanyId())) {
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
}
