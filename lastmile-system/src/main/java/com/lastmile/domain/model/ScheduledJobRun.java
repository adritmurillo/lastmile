package com.lastmile.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.With;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Representa el registro de ejecución de un trabajo programado.
 * Se usa para rastrear cuándo se ejecutó por última vez cada trabajo
 * y detectar trabajos perdidos al iniciar el servidor.
 */
@Getter
@Builder
@With
public class ScheduledJobRun {
    private final UUID id;
    private final String jobName;
    private final String cronExpression;
    private final LocalDateTime lastRunAt;
    private final JobRunStatus lastStatus;
    private final int runCount;
    private final String lastErrorMessage;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
    
    /**
     * Crea un nuevo registro para un trabajo que se ejecuta por primera vez.
     */
    public static ScheduledJobRun createNew(String jobName, String cronExpression) {
        LocalDateTime now = LocalDateTime.now();
        return ScheduledJobRun.builder()
                .id(UUID.randomUUID())
                .jobName(jobName)
                .cronExpression(cronExpression)
                .lastRunAt(now)
                .lastStatus(JobRunStatus.RUNNING)
                .runCount(1)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
    
    /**
     * Marca el trabajo como completado exitosamente.
     */
    public ScheduledJobRun markSuccess() {
        return this
                .withLastStatus(JobRunStatus.SUCCESS)
                .withLastRunAt(LocalDateTime.now())
                .withUpdatedAt(LocalDateTime.now())
                .withLastErrorMessage(null);
    }
    
    /**
     * Marca el trabajo como fallido con un mensaje de error.
     */
    public ScheduledJobRun markFailed(String errorMessage) {
        return this
                .withLastStatus(JobRunStatus.FAILED)
                .withLastRunAt(LocalDateTime.now())
                .withUpdatedAt(LocalDateTime.now())
                .withLastErrorMessage(errorMessage);
    }
    
    /**
     * Marca el trabajo como en ejecución e incrementa el contador.
     */
    public ScheduledJobRun markRunning() {
        return this
                .withLastStatus(JobRunStatus.RUNNING)
                .withRunCount(this.runCount + 1)
                .withUpdatedAt(LocalDateTime.now());
    }
}
