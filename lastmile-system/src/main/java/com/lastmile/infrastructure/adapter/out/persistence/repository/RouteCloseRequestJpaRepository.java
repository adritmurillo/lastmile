package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.infrastructure.adapter.out.persistence.entity.RouteCloseRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface RouteCloseRequestJpaRepository extends JpaRepository<RouteCloseRequestEntity, UUID> {
    
    List<RouteCloseRequestEntity> findByStatus(String status);
    
    List<RouteCloseRequestEntity> findByRouteId(UUID routeId);
    
    List<RouteCloseRequestEntity> findByCourierId(UUID courierId);
    
    @Query("SELECT r FROM RouteCloseRequestEntity r WHERE r.courier.id = :courierId AND r.status = :status AND r.createdAt >= :since ORDER BY r.createdAt DESC")
    List<RouteCloseRequestEntity> findByCourierIdAndStatusSince(
            @Param("courierId") UUID courierId,
            @Param("status") String status,
            @Param("since") LocalDateTime since
    );
    
    @Query("SELECT COUNT(r) FROM RouteCloseRequestEntity r WHERE r.courier.id = :courierId AND r.status = 'APPROVED' AND r.createdAt >= :since")
    int countApprovedByCourierIdSince(@Param("courierId") UUID courierId, @Param("since") LocalDateTime since);
    
    @Query("SELECT r FROM RouteCloseRequestEntity r LEFT JOIN FETCH r.route LEFT JOIN FETCH r.courier WHERE r.status = :status ORDER BY r.createdAt DESC")
    List<RouteCloseRequestEntity> findByStatusWithDetails(@Param("status") String status);
}
