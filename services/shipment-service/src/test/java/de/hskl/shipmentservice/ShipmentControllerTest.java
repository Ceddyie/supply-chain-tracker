package de.hskl.shipmentservice;

import de.hskl.shipmentservice.controller.ShipmentController;
import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.dto.ShipmentListItemDto;
import de.hskl.shipmentservice.exceptions.GlobalExceptionHandler;
import de.hskl.shipmentservice.service.ShipmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ShipmentController.class)
public class ShipmentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ShipmentService shipmentService;

    private CreateShipmentDto testCreateDto;
    private ShipmentDetailDto testDetailDto;
    private UUID testShipmentId;

    @BeforeEach
    void setUp() {
        testShipmentId = UUID.randomUUID();

        testCreateDto = new CreateShipmentDto(
                "Test Sender",
                "Test Receiver",
                Instant.now().plus(2, ChronoUnit.DAYS)
        );

        testDetailDto = new ShipmentDetailDto(
                testShipmentId,
                "Test Sender",
                "Test Receiver",
                "CREATED",
                testCreateDto.expectedDelivery(),
                null,
                null,
                List.of()
        );
    }

    @Test
    void createShipment_shouldReturnCreated() throws Exception {
        when(shipmentService.createShipment(any(CreateShipmentDto.class), anyString(), anyString())).thenReturn(testDetailDto);

        mockMvc.perform(post("/create")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(testShipmentId.toString()))
                .andExpect(jsonPath("$.sender").value("Test Sender"))
                .andExpect(jsonPath("$.receiver").value("Test Receiver"))
                .andExpect(jsonPath("$.currentStatus").value("CREATED"));
    }

    @Test
    void getShipment_whenExists_shouldReturnOk() throws Exception {
        when(shipmentService.getShipment(eq(testShipmentId), anyString(), anyString(), anyBoolean())).thenReturn(testDetailDto);

        mockMvc.perform(get("/" + testShipmentId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testShipmentId.toString()))
                .andExpect(jsonPath("$.sender").value("Test Sender"));
    }

    @Test
    void getShipment_whenNotFound_shouldReturn404() throws Exception {
        UUID nonExistentId = UUID.randomUUID();
        when(shipmentService.getShipment(eq(nonExistentId), anyString(), anyString(), anyBoolean())).thenThrow(new GlobalExceptionHandler.ShipmentNotFoundException(nonExistentId));

        mockMvc.perform(get("/" + nonExistentId)).andExpect(status().isNotFound());
    }

    @Test
    void getShipment_whenUnauthorized_shouldReturn403() throws Exception {
        when(shipmentService.getShipment(eq(testShipmentId), anyString(), anyString(), anyBoolean())).thenThrow(new GlobalExceptionHandler.AccessDeniedException("Forbidden"));

        mockMvc.perform(get("/" + testShipmentId)).andExpect(status().isForbidden());
    }

    @Test
    void listShipments_shouldReturnList() throws Exception {
        ShipmentListItemDto item1 = new ShipmentListItemDto(
                UUID.randomUUID(),
                "Sender 1",
                "Receiver 1",
                "CREATED",
                Instant.now()
        );
        ShipmentListItemDto item2 = new ShipmentListItemDto(
                UUID.randomUUID(),
                "Sender 2",
                "Receiver 2",
                "IN_TRANSIT",
                Instant.now()
        );

        when(shipmentService.listForUser(anyString())).thenReturn(List.of(item1, item2));

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].sender").value("Sender 1"))
                .andExpect(jsonPath("$[1].sender").value("Sender 2"));
    }

    @Test
    void listShipments_whenEmpty_shouldReturnEmptyArray() throws Exception {
        when(shipmentService.listForUser(anyString())).thenReturn(List.of());

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }
}
