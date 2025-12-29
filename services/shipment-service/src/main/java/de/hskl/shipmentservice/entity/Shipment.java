package de.hskl.shipmentservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Data
@Entity
@Table(name = "shipments")
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Shipment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false)
    private UUID id;

    @Column
    private String trackingId;

    @Column(name = "owner_user_id", length = 64)
    private String ownerUserId;

    @Column(name = "company_id", length = 64)
    private String companyId;

    @Column(name = "sender", nullable = false)
    private String sender;

    @Column(name = "receiver", nullable = false)
    private String receiver;

    @Column(name = "current_status", nullable = false, length = 50)
    private String currentStatus;

    @Column(name = "expected_delivery")
    private Instant expectedDelivery;

    @Column(name = "last_lat")
    private Double lastLat;

    @Column(name = "last_lng")
    private Double lastLng;

    @ColumnDefault("now()")
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ColumnDefault("now()")
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

}