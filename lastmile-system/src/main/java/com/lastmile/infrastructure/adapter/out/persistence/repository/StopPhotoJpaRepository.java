package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.infrastructure.adapter.out.persistence.entity.StopPhotoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface StopPhotoJpaRepository extends JpaRepository<StopPhotoEntity, UUID> {
    List<StopPhotoEntity> findByStopIdOrderByPhotoOrderAsc(UUID stopId);
}