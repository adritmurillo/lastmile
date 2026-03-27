package com.lastmile.domain.port.out;

import com.lastmile.domain.model.ScheduledJobRun;

import java.util.List;
import java.util.Optional;

/**
 * Puerto de salida para persistir registros de ejecución de trabajos programados.
 */
public interface ScheduledJobRunRepository {
    
    /**
     * Guarda o actualiza un registro de ejecución.
     */
    ScheduledJobRun save(ScheduledJobRun jobRun);
    
    /**
     * Busca un registro por nombre de trabajo.
     */
    Optional<ScheduledJobRun> findByJobName(String jobName);
    
    /**
     * Obtiene todos los registros de trabajos programados.
     */
    List<ScheduledJobRun> findAll();
    
    /**
     * Verifica si existe un registro para un trabajo específico.
     */
    boolean existsByJobName(String jobName);
}
