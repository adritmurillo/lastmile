package com.lastmile.infrastructure.adapter.out.persistence.repository;

import com.lastmile.domain.model.BulkLoadStatus;
import com.lastmile.infrastructure.adapter.out.persistence.entity.BulkLoadEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BulkLoadJpaRepository extends JpaRepository<BulkLoadEntity, UUID> {
    List<BulkLoadEntity> findByStatus(BulkLoadStatus status);
    List<BulkLoadEntity> findByUploadedByOrderByStartedAtDesc(String uploadedBy);
    @Query("""
            SELECT b FROM BulkLoadEntity b
            LEFT JOIN FETCH b.errors
            WHERE b.id = :id
            """)
    Optional<BulkLoadEntity> findByIdWithErrors(@Param("id") UUID id);
}
