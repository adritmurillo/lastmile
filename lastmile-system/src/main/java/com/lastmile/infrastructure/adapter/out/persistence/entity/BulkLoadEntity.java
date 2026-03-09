package com.lastmile.infrastructure.adapter.out.persistence.entity;

import com.lastmile.domain.model.BulkLoadStatus;
import com.lastmile.domain.model.LoadSource;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bulk_loads")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BulkLoadEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "file_name", nullable = false)
    private String fileName;

    @Enumerated(EnumType.STRING)
    @Column(name = "load_source", nullable = false)
    private LoadSource loadSource;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private BulkLoadStatus status;

    @Column(name = "total_records")
    private int totalRecords;

    @Column(name = "successful_records")
    private int successfulRecords;

    @Column(name = "failed_records")
    private int failedRecords;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Column(name = "uploaded_by", nullable = false)
    private String uploadedBy;

    @OneToMany(mappedBy = "bulkLoad", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BulkLoadErrorEntity> errors = new ArrayList<>();
}