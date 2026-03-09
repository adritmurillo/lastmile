package com.lastmile.infrastructure.adapter.out.persistence.repository;


import com.lastmile.domain.model.CourierStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.CourierEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CourierJpaRepository extends JpaRepository<CourierEntity, UUID> {
    List<CourierEntity> findByStatus(CourierStatus status);
    boolean existsByDocumentNumber(String documentNumber);
    @Query("SELECT c FROM CourierEntity c LEFT JOIN FETCH c.vehicle WHERE c.status = 'ACTIVE'")
    List<CourierEntity> findAvailableCouriersWithVehicle();

}
