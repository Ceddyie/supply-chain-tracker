package de.hskl.shipmentservice.repository;

import de.hskl.shipmentservice.entity.Checkpoint;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CheckpointRepository extends JpaRepository<Checkpoint, UUID> {
    List<Checkpoint> findByShipmentIdOrderByTimestampAsc(UUID shipmentId);
}
