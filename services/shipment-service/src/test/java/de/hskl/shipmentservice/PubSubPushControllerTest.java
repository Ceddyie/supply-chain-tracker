package de.hskl.shipmentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hskl.shipmentservice.controller.PubSubPushController;
import de.hskl.shipmentservice.service.ShipmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Base64;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PubSubPushController.class)
@ActiveProfiles("prod")
public class PubSubPushControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private ShipmentService shipmentService;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Test
    void receivePushMessage_shouldProcessValidMessage() throws Exception {
        String trackingPayload = objectMapper.writeValueAsString(Map.of(
                "shipmentId", UUID.randomUUID().toString(),
                "status", "IN_TRANSIT",
                "message", "Package scanned",
                "lat", 50.0,
                "lng", 8.0
        ));

        String encodedPayload = Base64.getEncoder().encodeToString(trackingPayload.getBytes());

        String pushMessage = objectMapper.writeValueAsString(Map.of(
                "message", Map.of(
                        "data", encodedPayload,
                        "messageId", "12345",
                        "publishTime", "2026-01-01T00:00:00Z"
                ),
                "subscription", "projects/test/subscriptions/tracking-updates-sub"
        ));

        doNothing().when(shipmentService).updateShipment(anyString());

        mockMvc.perform(post("/pubsub/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pushMessage))
                .andExpect(status().isOk())
                .andExpect(content().string("Message processed"));

        verify(shipmentService, times(1)).updateShipment(anyString());
    }

    @Test
    void receivePushMessage_withInvalidFormat_shouldReturnOkToAvoidRedelivery() throws Exception {
        String invalidMessage = "{ \"invalid\": \"format\" }";

        mockMvc.perform(post("/pubsub/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidMessage))
                .andExpect(status().isOk())
                .andExpect(content().string("Invalid message format"));

        verify(shipmentService, never()).updateShipment(anyString());
    }

    @Test
    void receivePushMessage_whenProcessingFails_shouldReturn500() throws Exception {
        String trackingPayload = "{ \"shipmentId\": \"invalid-uuid\" }";
        String encodedPayload = Base64.getEncoder().encodeToString(trackingPayload.getBytes());

        String pushMessage = objectMapper.writeValueAsString(Map.of(
                "message", Map.of(
                        "data", encodedPayload,
                        "messageId", "12345"
                )
        ));

        doThrow(new com.fasterxml.jackson.core.JsonProcessingException("Parse error") {})
                .when(shipmentService).updateShipment(anyString());

        mockMvc.perform(post("/pubsub/push")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pushMessage))
                .andExpect(status().isInternalServerError());
    }
}
