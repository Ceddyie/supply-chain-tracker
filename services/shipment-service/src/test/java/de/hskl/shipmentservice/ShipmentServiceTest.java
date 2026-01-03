package de.hskl.shipmentservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.dto.ShipmentListItemDto;
import de.hskl.shipmentservice.entity.Checkpoint;
import de.hskl.shipmentservice.entity.Shipment;
import de.hskl.shipmentservice.exceptions.GlobalExceptionHandler;
import de.hskl.shipmentservice.repository.CheckpointRepository;
import de.hskl.shipmentservice.repository.ShipmentRepository;
import de.hskl.shipmentservice.service.ShipmentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ShipmentServiceTest {
    @Mock
    private ShipmentRepository shipmentRepository;

    @Mock
    private CheckpointRepository checkpointRepository;

    @InjectMocks
    private ShipmentService shipmentService;

    private CreateShipmentDto testCreateDto;
    private Shipment testShipment;
    private Checkpoint testCheckpoint;
    private UUID testShipmentId;

    @BeforeEach
    void setUp() {
        testShipmentId = UUID.randomUUID();

        testCreateDto = new CreateShipmentDto(
                "Test Sender",
                "Test Receiver",
                "Musterstraße 1",
                "Musterstadt",
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        testShipment = Shipment.builder()
                .id(testShipmentId)
                .trackingId("PKG-1234ABCD")
                .ownerUserId("user-123")
                .receiverStreet(testCreateDto.receiverStreet())
                .receiverCity(testCreateDto.receiverCity())
                .sender(testCreateDto.sender())
                .receiver(testCreateDto.receiver())
                .currentStatus("CREATED")
                .expectedDelivery(testCreateDto.expectedDelivery())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        testCheckpoint = Checkpoint.builder()
                .id(UUID.randomUUID())
                .shipment(testShipment)
                .timestamp(Instant.now())
                .status("CREATED")
                .message("Shipment created")
                .build();
    }

    @Test
    void createShipment_shouldSaveShipmentAndCheckpoint() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(testCheckpoint);

        ShipmentDetailDto result = shipmentService.createShipment(testCreateDto, "user-123");

        assertNotNull(result);
        assertEquals("Test Sender", result.sender());
        assertEquals("Test Receiver", result.receiver());
        assertEquals("CREATED", result.currentStatus());
        assertEquals(1, result.timeline().size());

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
        verify(checkpointRepository, times(1)).save(any(Checkpoint.class));
    }

    @Test
    void createShipment_shouldSetCorrectInitialValues() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(testCheckpoint);

        shipmentService.createShipment(testCreateDto, "user-123");

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertEquals("user-123", savedShipment.getOwnerUserId());
        assertEquals("CREATED", savedShipment.getCurrentStatus());
        assertNotNull(savedShipment.getCreatedAt());
        assertNotNull(savedShipment.getUpdatedAt());
    }

    @Test
    void createShipment_shouldCreateInitialCheckpoint() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(testCheckpoint);

        shipmentService.createShipment(testCreateDto, "user-123");

        ArgumentCaptor<Checkpoint> checkpointCaptor = ArgumentCaptor.forClass(Checkpoint.class);
        verify(checkpointRepository).save(checkpointCaptor.capture());

        Checkpoint savedCheckpoint = checkpointCaptor.getValue();
        assertEquals("CREATED", savedCheckpoint.getStatus());
        assertEquals("Shipment created", savedCheckpoint.getMessage());
        assertNotNull(savedCheckpoint.getTimestamp());
    }

    @Test
    void getShipment_whenOwnerRequests_shouldReturnShipment() {
        when(shipmentRepository.findById(testShipmentId)).thenReturn(Optional.of(testShipment));
        when(checkpointRepository.findByShipmentIdOrderByTimestampAsc(testShipmentId)).thenReturn(List.of(testCheckpoint));

        ShipmentDetailDto result = shipmentService.getShipment(
                testShipmentId,
                "user-123",
                false
        );

        assertNotNull(result);
        assertEquals(testShipmentId, result.id());
        assertEquals("Test Sender", result.sender());
        assertEquals("Test Receiver", result.receiver());
        assertEquals(1, result.timeline().size());
    }

    @Test
    void getShipment_whenAdminRequests_shouldReturnShipment() {
        when(shipmentRepository.findById(testShipmentId)).thenReturn(Optional.of(testShipment));
        when(checkpointRepository.findByShipmentIdOrderByTimestampAsc(testShipmentId)).thenReturn(List.of(testCheckpoint));

        ShipmentDetailDto result = shipmentService.getShipment(
                testShipmentId,
                "any-user",
                true
        );

        assertNotNull(result);
        assertEquals(testShipmentId, result.id());
    }

    @Test
    void getShipment_whenUnauthorizedUserRequests_shouldThrowAccessDenied() {
        when(shipmentRepository.findById(testShipmentId)).thenReturn(Optional.of(testShipment));

        assertThrows(
                GlobalExceptionHandler.AccessDeniedException.class,
                () -> shipmentService.getShipment(
                        testShipmentId,
                        "unauth-user",
                        false
                )
        );
    }

    @Test
    void getShipment_whenNotFound_shouldThrowNotFoundException() {
        UUID nonExistentId = UUID.randomUUID();
        when(shipmentRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        assertThrows(
                GlobalExceptionHandler.ShipmentNotFoundException.class,
                () -> shipmentService.getShipment(
                        nonExistentId,
                        "user-123",
                        false
                )
        );
    }

    @Test
    void listForUser_shouldReturnUserShipments() {
        Shipment shipment2 = Shipment.builder()
                .id(UUID.randomUUID())
                .trackingId("PKG-ABCD1234")
                .ownerUserId("user-123")
                .sender("Sender 2")
                .receiver("Receiver 2")
                .receiverStreet("Receiver Street")
                .receiverCity("Receiver City")
                .currentStatus("IN TRANSIT")
                .createdAt(Instant.now())
                .build();

        when(shipmentRepository.findByOwnerUserId("user-123")).thenReturn(List.of(testShipment, shipment2));

        List<ShipmentListItemDto> result = shipmentService.listForUser("user-123");

        assertEquals(2, result.size());
        assertEquals("Test Sender", result.get(0).sender());
        assertEquals("Sender 2", result.get(1).sender());
        verify(shipmentRepository, times(1)).findByOwnerUserId("user-123");
    }

    @Test
    void listForUser_whenNoShipments_shouldReturnEmptyList() {
        when(shipmentRepository.findByOwnerUserId("user-123")).thenReturn(List.of());

        List<ShipmentListItemDto> result = shipmentService.listForUser("user-123");

        assertTrue(result.isEmpty());
    }
}