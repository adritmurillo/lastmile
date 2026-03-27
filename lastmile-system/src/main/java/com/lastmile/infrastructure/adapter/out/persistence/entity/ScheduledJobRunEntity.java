package com.lastmile.infrastructure.adapter.out.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad JPA para rastrear ejecuciones de trabajos programados.
 */
@Entity
@Table(name = "scheduled_job_runs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledJobRunEntity {
    
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "job_name", nullable = false, unique = true, length = 100)
    private String jobName;
    
    @Column(name = "cron_expression", nullable = false, length = 50)
    private String cronExpression;
    
    @Column(name = "last_run_at", nullable = false)
    private LocalDateTime lastRunAt;
    
    @Column(name = "last_status", nullable = false, length = 20)
    private String lastStatus;
    
    @Column(name = "run_count")
    private int runCount;
    
    @Column(name = "last_error_message", columnDefinition = "TEXT")
    private String lastErrorMessage;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
