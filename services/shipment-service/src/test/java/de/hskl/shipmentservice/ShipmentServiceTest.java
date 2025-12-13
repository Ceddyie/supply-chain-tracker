package de.hskl.shipmentservice;

import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.entity.Shipment;
import de.hskl.shipmentservice.repository.CheckpointRepository;
import de.hskl.shipmentservice.repository.ShipmentRepository;
import de.hskl.shipmentservice.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private CreateShipmentDto testCreateShipment;
    private Shipment testShipment;

    @BeforeEach
    void setUp() {
        testCreateShipment = new CreateShipmentDto(
                "Test Sender",
                "Test Receiver",
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        testShipment = new Shipment();
        testShipment.setId(UUID.randomUUID());
        testShipment.setOwnerUserId("user-123");
        testShipment.setCompanyId("company-123");
        testShipment.setSender(testCreateShipment.sender());
        testShipment.setReceiver(testCreateShipment.receiver());
        testShipment.setCurrentStatus("CREATED");
        testShipment.setExpectedDelivery(testCreateShipment.expectedDelivery());
        testShipment.setCreatedAt(Instant.now());
    }

    @Test
    void createShipment_shouldSaveAndReturnShipment() {
        when(shipmentRepository.save(any(Shipment.class))).thenReturn(testShipment);

        ShipmentDetailDto result = shipmentService.createShipment(testCreateShipment, "user-123", "company-123");

        assertNotNull(result);
        assertEquals("Test Sender", result.sender());
        assertEquals("Test Receiver", result.receiver());
        assertEquals("CREATED", result.currentStatus());

        verify(shipmentRepository, times(1)).save(any(Shipment.class));
    }
}
