package com.lastmile.domain.model;

/**
 * Estados posibles para la ejecución de un trabajo programado.
 */
public enum JobRunStatus {
    /** El trabajo se ejecutó exitosamente */
    SUCCESS,
    
    /** El trabajo falló durante la ejecución */
    FAILED,
    
    /** El trabajo está actualmente en ejecución */
    RUNNING
}
