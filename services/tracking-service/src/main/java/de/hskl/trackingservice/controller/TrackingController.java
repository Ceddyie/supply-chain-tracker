package de.hskl.trackingservice.controller;

import de.hskl.trackingservice.dto.TrackingUpdateDto;
import de.hskl.trackingservice.service.TrackingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
public class TrackingController {
    private final TrackingService trackingService;

    @PostMapping("/update")
    public ResponseEntity<String> updateTracking(@RequestBody TrackingUpdateDto dto) {
        log.info("Received tracking update for shipment: {}", dto.shipmentId());

        trackingService.processTrackingUpdate(dto);

        return ResponseEntity.ok("Tracking update received");
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Tracking Service is running");
    }
}
