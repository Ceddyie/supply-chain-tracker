package de.hskl.shipmentservice.controller;

import de.hskl.shipmentservice.dto.CreateShipmentDto;
import de.hskl.shipmentservice.dto.ShipmentDetailDto;
import de.hskl.shipmentservice.dto.ShipmentListItemDto;
import de.hskl.shipmentservice.service.ShipmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ShipmentController {
    private final ShipmentService shipmentService;

    // TODO: später durch echte Werte aus API Request ersetzen (Firebase Login)
    private String mockUserId() { return "user-123"; }
    private String mockCompanyId() { return "company-abc"; }
    private boolean mockIsAdmin() { return false; }

    @PostMapping("/create")
    public ResponseEntity<ShipmentDetailDto> create(@RequestBody CreateShipmentDto dto) {
        ShipmentDetailDto created = shipmentService.createShipment(dto, mockUserId(), mockCompanyId());

        URI location = URI.create("/" + created.id());
        return ResponseEntity.created(location).body(created); // Code 201 CREATED
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShipmentDetailDto> get(@PathVariable UUID id) {
        ShipmentDetailDto dto = shipmentService.getShipment(id, mockUserId(), mockCompanyId(), mockIsAdmin());
        return ResponseEntity.ok(dto);
    }

    @GetMapping
    public List<ShipmentListItemDto> listMine() {
        return shipmentService.listForUser(mockUserId());
    }
}
