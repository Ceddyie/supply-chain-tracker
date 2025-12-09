package de.hskl.shipmentservice.repository;

import de.hskl.shipmentservice.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {
    List<Shipment> findByOwnerUserId(String ownerUserId);
    List<Shipment> findByCompanyId(String companyId);
}
