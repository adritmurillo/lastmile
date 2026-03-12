package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.domain.model.StopStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.StopEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface StopJpaRepository extends JpaRepository<StopEntity, UUID> {
    List<StopEntity> findByRouteIdOrderByStopOrderAsc(UUID routeId);
    List<StopEntity> findByStatus(StopStatus status);

    @Query("""
            SELECT s FROM StopEntity s
            LEFT JOIN FETCH s.order
            LEFT JOIN FETCH s.route
            WHERE s.id = :stopId
            """)
    Optional<StopEntity> findByIdWithDetails(@Param("stopId") UUID stopId);

    @Query("""
    SELECT s FROM StopEntity s
    LEFT JOIN FETCH s.order o
    LEFT JOIN FETCH s.route r
    LEFT JOIN FETCH r.courier c
    WHERE c.id = :courierId
    AND s.status = 'PENDING'
    AND o.status != 'RETURNED'
    AND o.deliveryAttempts < 3
    AND r.date < :today
    AND r.status != 'CANCELLED'
    ORDER BY r.date ASC
    """)
    List<StopEntity> findPendingByCourierBeforeDate(
            @Param("courierId") UUID courierId,
            @Param("today") LocalDate today);
}
