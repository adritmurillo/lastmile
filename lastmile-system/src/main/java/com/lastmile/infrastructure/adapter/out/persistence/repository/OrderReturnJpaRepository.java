package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.infrastructure.adapter.out.persistence.entity.OrderReturnEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderReturnJpaRepository extends JpaRepository<OrderReturnEntity, UUID> {
    
    List<OrderReturnEntity> findByOrderId(UUID orderId);
    
    List<OrderReturnEntity> findByCourierId(UUID courierId);
    
    List<OrderReturnEntity> findByRouteId(UUID routeId);
    
    @Query("SELECT r FROM OrderReturnEntity r WHERE r.order.id = :orderId AND r.courier.id = :courierId AND r.returnedAt = :date")
    List<OrderReturnEntity> findByOrderIdAndCourierIdAndDate(
            @Param("orderId") UUID orderId,
            @Param("courierId") UUID courierId,
            @Param("date") LocalDate date
    );
    
    @Query("SELECT DISTINCT r.courier.id FROM OrderReturnEntity r WHERE r.order.id = :orderId AND r.returnedAt = :date")
    List<UUID> findCourierIdsByOrderIdAndDate(@Param("orderId") UUID orderId, @Param("date") LocalDate date);
    
    @Query("SELECT r FROM OrderReturnEntity r WHERE r.returnedAt = :date")
    List<OrderReturnEntity> findByReturnedAt(@Param("date") LocalDate date);
}
