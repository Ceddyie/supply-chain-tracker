package de.hskl.shipmentservice;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.dto.ShipmentListItemDto;
import de.hskl.shipmentservice.entity.Checkpoint;
import de.hskl.shipmentservice.entity.Shipment;
import de.hskl.shipmentservice.entity.TrackingUpdate;
import de.hskl.shipmentservice.exceptions.GlobalExceptionHandler;
import de.hskl.shipmentservice.repository.CheckpointRepository;
import de.hskl.shipmentservice.repository.ShipmentRepository;
import de.hskl.shipmentservice.service.ShipmentService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
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

    @Captor
    private ArgumentCaptor<Shipment> shipmentCaptor;

    @Captor
    private ArgumentCaptor<Checkpoint> checkpointCaptor;

    private ObjectMapper objectMapper;

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
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        testShipment = Shipment.builder()
                .id(testShipmentId)
                .ownerUserId("user-123")
                .companyId("company-abc")
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

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void createShipment_shouldSaveShipmentAndCheckpoint() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(testCheckpoint);

        ShipmentDetailDto result = shipmentService.createShipment(testCreateDto, "user-123", "company-123");

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

        shipmentService.createShipment(testCreateDto, "user-123", "company-abc");

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(shipmentRepository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertEquals("user-123", savedShipment.getOwnerUserId());
        assertEquals("company-abc", savedShipment.getCompanyId());
        assertEquals("CREATED", savedShipment.getCurrentStatus());
        assertNotNull(savedShipment.getCreatedAt());
        assertNotNull(savedShipment.getUpdatedAt());
    }

    @Test
    void createShipment_shouldCreateInitialCheckpoint() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);
        when(checkpointRepository.save(any(Checkpoint.class))).thenReturn(testCheckpoint);

        shipmentService.createShipment(testCreateDto, "user-123", "company-abc");

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
                "company-abc",
                false
        );

        assertNotNull(result);
        assertEquals(testShipmentId, result.id());
        assertEquals("Test Sender", result.sender());
        assertEquals("Test Receiver", result.receiver());
        assertEquals(1, result.timeline().size());
    }

    @Test
    void getShipment_whenCompanyMemberRequests_shouldReturnShipment() {
        when(shipmentRepository.findById(testShipmentId)).thenReturn(Optional.of(testShipment));
        when(checkpointRepository.findByShipmentIdOrderByTimestampAsc(testShipmentId)).thenReturn(List.of(testCheckpoint));

        ShipmentDetailDto result = shipmentService.getShipment(
                testShipmentId,
                "user-abc",
                "company-abc",
                false
        );

        assertNotNull(result);
        assertEquals(testShipmentId, result.id());
    }

    @Test
    void getShipment_whenAdminRequests_shouldReturnShipment() {
        when(shipmentRepository.findById(testShipmentId)).thenReturn(Optional.of(testShipment));
        when(checkpointRepository.findByShipmentIdOrderByTimestampAsc(testShipmentId)).thenReturn(List.of(testCheckpoint));

        ShipmentDetailDto result = shipmentService.getShipment(
                testShipmentId,
                "any-user",
                "any-company",
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
                        "different-company",
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
                        "company-abc",
                        false
                )
        );
    }

    @Test
    void listForUser_shouldReturnUserShipments() {
        Shipment shipment2 = Shipment.builder()
                .id(UUID.randomUUID())
                .ownerUserId("user-123")
                .companyId("company-abc")
                .sender("Sender 2")
                .receiver("Receiver 2")
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

    @Test
    void updateShipment_WithValidPayload_ShouldUpdateShipmentAndCreateCheckpoint() throws JsonProcessingException, JsonProcessingException {
        UUID shipmentId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        TrackingUpdate trackingUpdate = new TrackingUpdate();
        trackingUpdate.setShipmentId(shipmentId);
        trackingUpdate.setStatus("IN_TRANSIT");
        trackingUpdate.setMessage("Package arrived at distribution center");
        trackingUpdate.setLat(49.2401);
        trackingUpdate.setLng(7.3607);
        trackingUpdate.setTimestamp(timestamp);

        String payload = objectMapper.writeValueAsString(trackingUpdate);

        Shipment existingShipment = new Shipment();
        existingShipment.setId(shipmentId);
        existingShipment.setCurrentStatus("PENDING");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkpointRepository.save(any(Checkpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.updateShipment(payload);

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository).save(shipmentCaptor.capture());
        verify(checkpointRepository).save(checkpointCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertThat(savedShipment.getCurrentStatus()).isEqualTo("IN_TRANSIT");
        assertThat(savedShipment.getLastLat()).isEqualTo(49.2401);
        assertThat(savedShipment.getLastLng()).isEqualTo(7.3607);
        assertThat(savedShipment.getUpdatedAt()).isNotNull();

        Checkpoint savedCheckpoint = checkpointCaptor.getValue();
        assertThat(savedCheckpoint.getShipment()).isEqualTo(existingShipment);
        assertThat(savedCheckpoint.getStatus()).isEqualTo("IN_TRANSIT");
        assertThat(savedCheckpoint.getMessage()).isEqualTo("Package arrived at distribution center");
        assertThat(savedCheckpoint.getLat()).isEqualTo(49.2401);
        assertThat(savedCheckpoint.getLng()).isEqualTo(7.3607);
        assertThat(savedCheckpoint.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void updateShipment_WithNonExistentShipment_ShouldThrowException() throws JsonProcessingException {
        UUID shipmentId = UUID.randomUUID();

        TrackingUpdate trackingUpdate = new TrackingUpdate();
        trackingUpdate.setShipmentId(shipmentId);
        trackingUpdate.setStatus("IN_TRANSIT");
        trackingUpdate.setMessage("Package in transit");
        trackingUpdate.setLat(49.2401);
        trackingUpdate.setLng(7.3607);
        trackingUpdate.setTimestamp(Instant.now());

        String payload = objectMapper.writeValueAsString(trackingUpdate);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> shipmentService.updateShipment(payload))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Shipment not found")
                .hasMessageContaining(shipmentId.toString());

        verify(shipmentRepository).findById(shipmentId);
        verify(shipmentRepository, never()).save(any());
        verify(checkpointRepository, never()).save(any());
    }

    @Test
    void updateShipment_WithInvalidJson_ShouldThrowException() {
        String invalidPayload = "{ invalid json }";

        assertThatThrownBy(() -> shipmentService.updateShipment(invalidPayload))
                .isInstanceOf(JsonProcessingException.class);

        verify(shipmentRepository, never()).findById(any());
        verify(shipmentRepository, never()).save(any());
        verify(checkpointRepository, never()).save(any());
    }

    @Test
    void updateShipment_WithNullCoordinates_ShouldUpdateShipment() throws JsonProcessingException {
        UUID shipmentId = UUID.randomUUID();
        Instant timestamp = Instant.now();

        TrackingUpdate trackingUpdate = new TrackingUpdate();
        trackingUpdate.setShipmentId(shipmentId);
        trackingUpdate.setStatus("DELIVERED");
        trackingUpdate.setMessage("Package delivered");
        trackingUpdate.setLat(null);
        trackingUpdate.setLng(null);
        trackingUpdate.setTimestamp(timestamp);

        String payload = objectMapper.writeValueAsString(trackingUpdate);

        Shipment existingShipment = new Shipment();
        existingShipment.setId(shipmentId);
        existingShipment.setCurrentStatus("IN_TRANSIT");
        existingShipment.setLastLat(49.2401);
        existingShipment.setLastLng(7.3607);

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkpointRepository.save(any(Checkpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        shipmentService.updateShipment(payload);

        verify(shipmentRepository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertThat(savedShipment.getCurrentStatus()).isEqualTo("DELIVERED");
        assertThat(savedShipment.getLastLat()).isNull();
        assertThat(savedShipment.getLastLng()).isNull();
    }

    @Test
    void updateShipment_WithStatusProgression_ShouldTrackAllStatuses() throws JsonProcessingException {
        UUID shipmentId = UUID.randomUUID();

        Shipment existingShipment = new Shipment();
        existingShipment.setId(shipmentId);
        existingShipment.setCurrentStatus("PENDING");

        when(shipmentRepository.findById(shipmentId)).thenReturn(Optional.of(existingShipment));
        when(shipmentRepository.save(any(Shipment.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(checkpointRepository.save(any(Checkpoint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String[] statuses = {"PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"};

        for (String status : statuses) {
            TrackingUpdate trackingUpdate = new TrackingUpdate();
            trackingUpdate.setShipmentId(shipmentId);
            trackingUpdate.setStatus(status);
            trackingUpdate.setMessage("Status changed to " + status);
            trackingUpdate.setLat(49.2401);
            trackingUpdate.setLng(7.3607);
            trackingUpdate.setTimestamp(Instant.now());

            String payload = objectMapper.writeValueAsString(trackingUpdate);

            shipmentService.updateShipment(payload);

            verify(shipmentRepository, atLeastOnce()).save(shipmentCaptor.capture());
            assertThat(shipmentCaptor.getValue().getCurrentStatus()).isEqualTo(status);
        }

        verify(checkpointRepository, times(4)).save(any(Checkpoint.class));
    }
}