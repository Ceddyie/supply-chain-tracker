package de.hskl.trackingservice.controller;

import de.hskl.trackingservice.dto.TrackingUpdateDto;
import de.hskl.trackingservice.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*")
@RestController
@RequiredArgsConstructor
@Slf4j
public class TrackingController {
    private final TrackingService trackingService;

    @PostMapping("/update")
    public ResponseEntity<String> updateTracking(@RequestBody TrackingUpdateDto dto) {
        log.info("Received tracking update for shipment: {}", dto.shipmentId());

        try {
            trackingService.processTrackingUpdate(dto);
            log.info("Successfully sent tracking update for shipment: {}", dto.shipmentId());
            return ResponseEntity.ok("Tracking update received");
        } catch (Exception e) {
            log.error("Failed to process tracking update for shipment: {}", dto.shipmentId(), e);
            return ResponseEntity.internalServerError()
                    .body("Failed to process tracking update: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tracking Service is running");
    }
}
