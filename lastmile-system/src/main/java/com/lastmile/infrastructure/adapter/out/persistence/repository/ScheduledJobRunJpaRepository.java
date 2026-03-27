package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.infrastructure.adapter.out.persistence.entity.ScheduledJobRunEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ScheduledJobRunJpaRepository extends JpaRepository<ScheduledJobRunEntity, UUID> {
    
    Optional<ScheduledJobRunEntity> findByJobName(String jobName);
    
    boolean existsByJobName(String jobName);
}
