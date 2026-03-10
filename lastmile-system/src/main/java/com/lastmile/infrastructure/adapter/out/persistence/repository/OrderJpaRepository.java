package com.lastmile.infrastructure.adapter.out.persistence.repository;


import com.lastmile.domain.model.OrderStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderJpaRepository extends JpaRepository<OrderEntity, UUID> {
    Optional<OrderEntity> findByTrackingCode(String trackingCode);
    Optional<OrderEntity> findByExternalTrackingCode(String externalTrackingCode);
    List<OrderEntity> findByStatus(OrderStatus status);
    boolean existsByExternalTrackingCode(String externalTrackingCode);

    @Query("""
        SELECT o FROM OrderEntity o
        WHERE o.status = 'PENDING'
        ORDER BY o.priority ASC, o.createdAt ASC
        """)
    List<OrderEntity> findPendingOrdersForDate(@Param("date") LocalDate date);

    @Query("""
            SELECT o FROM OrderEntity o
            WHERE o.status = 'FAILED'
            AND o.deliveryAttempts < 3
            """)
    List<OrderEntity> findReschedulableFailedOrders();

    @Query("""
            UPDATE OrderEntity o SET o.status = :status
            WHERE o.id IN :ids
            """)
    @Modifying
    @Transactional
    void updateStatusBatch(@Param("status") OrderStatus status, @Param("ids") List<UUID> ids);

    @Query("""
    SELECT o FROM OrderEntity o
    WHERE o.deliveryDeadline >= :startDate
    AND o.deliveryDeadline <= :endDate
    """)
    List<OrderEntity> findByDeliveryDeadlineBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

}
