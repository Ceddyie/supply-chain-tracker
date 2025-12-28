package de.hskl.trackingservice;

import de.hskl.trackingservice.controller.TrackingController;
import de.hskl.trackingservice.dto.TrackingUpdateDto;
import de.hskl.trackingservice.service.TrackingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrackingController.class)
public class TrackingControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private TrackingService trackingService;

    @Test
    void updateTracking_shouldReturnOk() throws Exception {
        TrackingUpdateDto dto = new TrackingUpdateDto(
                UUID.randomUUID(),
                "IN_TRANSIT",
                "Package scanned",
                50.0,
                8.0,
                Instant.now()
        );

        doNothing().when(trackingService).processTrackingUpdate(any());

        mockMvc.perform(post("/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("Tracking update received"));

        verify(trackingService, times(1)).processTrackingUpdate(any());
    }

    @Test
    void health_shouldReturnOk() throws Exception {
        mockMvc.perform(get("/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("Tracking Service is running"));
    }
}
