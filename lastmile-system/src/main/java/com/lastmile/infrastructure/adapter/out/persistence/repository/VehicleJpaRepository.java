package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.domain.model.VehicleStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.VehicleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface VehicleJpaRepository extends JpaRepository<VehicleEntity, UUID> {
    Optional<VehicleEntity> findByLicensePlate(String licensePlate);
    List<VehicleEntity> findByStatus(VehicleStatus status);
    boolean existsByLicensePlate(String licensePlate);
}
