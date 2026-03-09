package com.lastmile.infrastructure.adapter.out.persistence.entity;

import com.lastmile.domain.model.FailureReason;
import com.lastmile.domain.model.StopStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "stops", indexes = {
        @Index(name = "idx_stops_route_id", columnList = "route_id"),
        @Index(name = "idx_stops_order_id", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StopEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id", nullable = false)
    private RouteEntity route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "stop_order", nullable = false)
    private int stopOrder;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private StopStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "failure_reason")
    private FailureReason failureReason;

    @Column(name = "proof_photo_url")
    private String proofPhotoUrl;
}