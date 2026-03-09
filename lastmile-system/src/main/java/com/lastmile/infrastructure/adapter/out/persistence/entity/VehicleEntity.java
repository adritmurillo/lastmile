package com.lastmile.infrastructure.adapter.out.persistence.entity;

import com.lastmile.domain.model.VehicleStatus;
import com.lastmile.domain.model.VehicleType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "vehicles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleEntity {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "license_plate", nullable = false, unique = true)
    private String licensePlate;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private VehicleType type;

    @Column(name = "max_weight_kg", nullable = false)
    private Double maxWeightKg;

    @Column(name = "max_volume_cm3", nullable = false)
    private Double maxVolumeCm3;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private VehicleStatus status;
}
