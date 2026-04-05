package com.lastmile.domain.service;

import com.lastmile.domain.model.JobRunStatus;
import com.lastmile.domain.model.ScheduledJobRun;
import com.lastmile.domain.port.out.ScheduledJobRunRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Servicio de dominio para gestionar el tracking de trabajos programados.
 * Se encarga de registrar ejecuciones y determinar si un trabajo debe ejecutarse
 * durante la reconciliación al inicio del servidor.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ScheduledJobService {
    
    private final ScheduledJobRunRepository jobRunRepository;
    
    /**
     * Registra el inicio de un trabajo programado.
     * @return El registro de ejecución del trabajo
     */
    public ScheduledJobRun recordJobStart(String jobName, String cronExpression) {
        Optional<ScheduledJobRun> existing = jobRunRepository.findByJobName(jobName);
        
        ScheduledJobRun jobRun;
        if (existing.isPresent()) {
            jobRun = existing.get().markRunning();
        } else {
            jobRun = ScheduledJobRun.createNew(jobName, cronExpression);
        }
        
        return jobRunRepository.save(jobRun);
    }
    
    /**
     * Registra que un trabajo terminó exitosamente.
     */
    public void recordJobSuccess(String jobName) {
        jobRunRepository.findByJobName(jobName).ifPresent(jobRun -> {
            ScheduledJobRun updated = jobRun.markSuccess();
            jobRunRepository.save(updated);
            log.info("Job '{}' completed successfully. Total runs: {}", jobName, updated.getRunCount());
        });
    }
    
    /**
     * Registra que un trabajo falló.
     */
    public void recordJobFailure(String jobName, String errorMessage) {
        jobRunRepository.findByJobName(jobName).ifPresent(jobRun -> {
            ScheduledJobRun updated = jobRun.markFailed(errorMessage);
            jobRunRepository.save(updated);
            log.warn("Job '{}' failed: {}", jobName, errorMessage);
        });
    }
    
    /**
     * Determina si un trabajo debe ejecutarse durante la reconciliación de inicio.
     * Un trabajo debe ejecutarse si:
     * 1. Nunca se ha ejecutado antes, O
     * 2. La última ejecución exitosa fue ANTES del horario programado de hoy
     * 
     * @param jobName Nombre del trabajo
     * @param scheduledHour Hora programada (0-23)
     * @param scheduledMinute Minuto programado (0-59)
     * @return true si el trabajo debe ejecutarse
     */
    public boolean shouldRunOnStartup(String jobName, int scheduledHour, int scheduledMinute) {
        Optional<ScheduledJobRun> lastRun = jobRunRepository.findByJobName(jobName);
        
        if (lastRun.isEmpty()) {
            log.info("Job '{}' has never run - should execute on startup", jobName);
            return true;
        }
        
        ScheduledJobRun run = lastRun.get();
        
        // Si el trabajo está en estado RUNNING, no lo ejecutamos (puede estar atascado)
        if (run.getLastStatus() == JobRunStatus.RUNNING) {
            log.warn("Job '{}' is in RUNNING state - may be stuck. Skipping startup execution.", jobName);
            return false;
        }
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime todayScheduledTime = now.toLocalDate().atTime(LocalTime.of(scheduledHour, scheduledMinute));
        
        // Si ya pasó la hora programada de hoy
        if (now.isAfter(todayScheduledTime)) {
            // Y la última ejecución fue antes de la hora programada de hoy
            if (run.getLastRunAt().isBefore(todayScheduledTime)) {
                log.info("Job '{}' missed today's scheduled run at {}:{} (last ran: {})", 
                        jobName, scheduledHour, scheduledMinute, run.getLastRunAt());
                return true;
            }
        }
        
        log.debug("Job '{}' is up to date. Last run: {}", jobName, run.getLastRunAt());
        return false;
    }
    
    /**
     * Obtiene el registro de ejecución de un trabajo.
     */
    public Optional<ScheduledJobRun> getJobRun(String jobName) {
        return jobRunRepository.findByJobName(jobName);
    }
}
