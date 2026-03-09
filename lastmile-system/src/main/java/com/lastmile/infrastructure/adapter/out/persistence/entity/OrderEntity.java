package com.lastmile.infrastructure.adapter.out.persistence.entity;

import com.lastmile.domain.model.LoadSource;
import com.lastmile.domain.model.OrderPriority;
import com.lastmile.domain.model.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_tracking_code", columnList = "tracking_code"),
        @Index(name = "idx_orders_external_tracking", columnList = "external_tracking_code"),
        @Index(name = "idx_orders_delivery_deadline", columnList = "delivery_deadline")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "tracking_code", nullable = false, unique = true)
    private String trackingCode;

    @Column(name = "external_tracking_code", unique = true)
    private String externalTrackingCode;

    @Column(name = "platform_order_number")
    private String platformOrderNumber;

    @Column(name = "recipient_name", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "address_text", nullable = false)
    private String addressText;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "weight_kg", nullable = false)
    private Double weightKg;

    @Column(name = "volume_cm3", nullable = false)
    private Double volumeCm3;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private OrderPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status;

    @Column(name = "delivery_attempts", nullable = false)
    private int deliveryAttempts;

    @Column(name = "delivery_deadline")
    private LocalDate deliveryDeadline;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_source", nullable = false)
    private LoadSource loadSource;
}