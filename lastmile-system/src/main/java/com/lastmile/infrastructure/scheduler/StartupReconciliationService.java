package com.lastmile.infrastructure.scheduler;

import com.lastmile.domain.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * Servicio de reconciliación que se ejecuta al iniciar la aplicación.
 * 
 * Verifica si algún trabajo programado no se ejecutó mientras el servidor
 * estuvo apagado y lo ejecuta si es necesario.
 * 
 * Ejemplo: Si el servidor estaba apagado a las 5 AM, el trabajo de
 * re-agenda de órdenes no se ejecutó. Este servicio detecta
 * esa situación y ejecuta el trabajo al iniciar.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StartupReconciliationService {
    
    private final ScheduledJobService scheduledJobService;
    private final DailyScheduledTasks dailyScheduledTasks;
    
    /**
     * Se ejecuta cuando la aplicación está completamente lista.
     * Verifica y ejecuta trabajos programados que se perdieron.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("=== INICIO: Reconciliación de trabajos programados al inicio ===");
        
        try {
            reconcileRescheduleOrdersJob();
            reconcileAutoCloseRoutesJob();
            
            log.info("=== FIN: Reconciliación completada ===");
        } catch (Exception e) {
            log.error("Error durante la reconciliación de inicio: {}", e.getMessage(), e);
            // No lanzamos la excepción para no impedir que la aplicación inicie
        }
    }
    
    /**
     * Verifica y ejecuta el trabajo de re-agenda de órdenes si se perdió.
     */
    private void reconcileRescheduleOrdersJob() {
        String jobName = DailyScheduledTasks.JOB_RESCHEDULE_ORDERS;
        int scheduledHour = DailyScheduledTasks.RESCHEDULE_HOUR;
        int scheduledMinute = DailyScheduledTasks.RESCHEDULE_MINUTE;
        
        log.debug("Verificando trabajo '{}' (programado para {}:{})", 
                jobName, scheduledHour, scheduledMinute);
        
        if (scheduledJobService.shouldRunOnStartup(jobName, scheduledHour, scheduledMinute)) {
            log.info("Ejecutando trabajo perdido: {}", jobName);
            
            try {
                int rescheduledCount = dailyScheduledTasks.executeRescheduleOrders();
                log.info("Trabajo '{}' ejecutado exitosamente en reconciliación. {} órdenes re-agendadas.", 
                        jobName, rescheduledCount);
            } catch (Exception e) {
                log.error("Error al ejecutar trabajo '{}' durante reconciliación: {}", 
                        jobName, e.getMessage(), e);
            }
        } else {
            log.debug("Trabajo '{}' está al día, no requiere reconciliación", jobName);
        }
    }

    /**
     * Verifica y ejecuta el trabajo de cierre automático de rutas si se perdió.
     */
    private void reconcileAutoCloseRoutesJob() {
        String jobName = DailyScheduledTasks.JOB_AUTO_CLOSE_ROUTES;
        int scheduledHour = DailyScheduledTasks.AUTO_CLOSE_HOUR;
        int scheduledMinute = DailyScheduledTasks.AUTO_CLOSE_MINUTE;
        
        log.debug("Verificando trabajo '{}' (programado para {}:{})", 
                jobName, scheduledHour, scheduledMinute);
        
        if (scheduledJobService.shouldRunOnStartup(jobName, scheduledHour, scheduledMinute)) {
            log.info("Ejecutando trabajo perdido: {}", jobName);
            
            try {
                int closedCount = dailyScheduledTasks.executeAutoCloseRoutes();
                log.info("Trabajo '{}' ejecutado exitosamente en reconciliación. {} rutas cerradas.", 
                        jobName, closedCount);
            } catch (Exception e) {
                log.error("Error al ejecutar trabajo '{}' durante reconciliación: {}", 
                        jobName, e.getMessage(), e);
            }
        } else {
            log.debug("Trabajo '{}' está al día, no requiere reconciliación", jobName);
        }
    }
}
