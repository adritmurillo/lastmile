package com.lastmile.infrastructure.adapter.out.persistence.entity;

import com.lastmile.domain.model.RouteStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "routes", indexes = {
        @Index(name = "idx_routes_date", columnList = "date"),
        @Index(name = "idx_routes_courier_date", columnList = "courier_id, date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RouteEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "courier_id", nullable = false)
    private CourierEntity courier;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private RouteStatus status;

    @Column(name = "total_weight_kg")
    private Double totalWeightKg;

    @Column(name = "total_volume_cm3")
    private Double totalVolumeCm3;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("stopOrder ASC")
    @Builder.Default
    private List<StopEntity> stops = new ArrayList<>();
}