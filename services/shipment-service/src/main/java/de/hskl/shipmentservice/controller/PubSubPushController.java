package de.hskl.shipmentservice.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hskl.shipmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;

@RestController
@RequestMapping("/pubsub")
@Slf4j
@Profile("prod")
public class PubSubPushController {
    private final ShipmentService shipmentService;
    private final ObjectMapper objectMapper;

    public PubSubPushController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    @PostMapping("/push")
    public ResponseEntity<String> receivePushMessage(@RequestBody String body) {
        log.info("Received push message: {}", body);

        try {
            JsonNode root = objectMapper.readTree(body);
            JsonNode messageNode = root.get("message");

            if (messageNode == null || messageNode.get("data") == null) {
                log.warn("Invalid Pub/Sub message format: missing 'message' or 'data' field");
                return ResponseEntity.ok("Invalid message format"); // 200, dass nicht dauerhaft erneut gesendet wird
            }

            String encodedData = messageNode.get("data").asText();
            String payload = new String(Base64.getDecoder().decode(encodedData));

            log.info("Decoded message payload: {}", payload);

            shipmentService.updateShipment(payload);

            return ResponseEntity.ok("Message processed"); // 200 für ACK
        } catch (JsonProcessingException e) {
            log.error("Error processing Pub/Sub message", e);
            return ResponseEntity.internalServerError().body("Processing error"); // 500, um neue Nachricht zu erhalten
        }
    }
}
