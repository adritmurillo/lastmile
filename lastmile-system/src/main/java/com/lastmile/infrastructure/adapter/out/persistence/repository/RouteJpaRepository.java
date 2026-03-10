package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.domain.model.RouteStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RouteJpaRepository extends JpaRepository<RouteEntity, UUID> {

    List<RouteEntity> findByDate(LocalDate date);
    List<RouteEntity> findByStatus(RouteStatus status);

    @Query("""
        SELECT DISTINCT r FROM RouteEntity r
        LEFT JOIN FETCH r.stops s
        LEFT JOIN FETCH s.order
        LEFT JOIN FETCH r.courier c
        LEFT JOIN FETCH c.vehicle
        WHERE r.courier.id = :courierId
        AND r.date = :date
        AND r.status IN ('PENDING','CONFIRMED','IN_PROGRESS')
        """)
    Optional<RouteEntity> findActiveCourierRoute(
            @Param("courierId") UUID courierId,
            @Param("date") LocalDate date);

    @Query("""
            SELECT DISTINCT r FROM RouteEntity r
            LEFT JOIN FETCH r.stops s
            LEFT JOIN FETCH s.order
            LEFT JOIN FETCH r.courier c
            LEFT JOIN FETCH c.vehicle
            WHERE r.date = :date
            ORDER BY r.status ASC
            """)
    List<RouteEntity> findByDateWithDetails(@Param("date") LocalDate date);

    @Query("""
    SELECT DISTINCT r FROM RouteEntity r
    LEFT JOIN FETCH r.stops s
    LEFT JOIN FETCH s.order o
    LEFT JOIN FETCH r.courier c
    LEFT JOIN FETCH c.vehicle
    WHERE o.id = :orderId
    """)
    Optional<RouteEntity> findByOrderId(@Param("orderId") UUID orderId);

}