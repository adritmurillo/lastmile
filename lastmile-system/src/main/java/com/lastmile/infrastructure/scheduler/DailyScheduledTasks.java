package com.lastmile.infrastructure.scheduler;

import com.lastmile.domain.port.in.ExecuteRouteUseCase;
import com.lastmile.domain.port.in.ManageOrdersUseCase;
import com.lastmile.domain.service.ScheduledJobService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Tareas programadas que se ejecutan automáticamente.
 * 
 * - Re-agenda de órdenes: 5:00 AM diario
 *   - FAILED (intentos < 3) → PENDING
 *   - SKIPPED → PENDING  
 *   - RETURNED_TO_WAREHOUSE → PENDING
 * 
 * - Cierre automático de rutas: 9:00 PM diario
 *   - Rutas CONFIRMED o IN_PROGRESS → CLOSED
 *   - Paradas PENDING → SKIPPED
 *   - Órdenes en esas paradas → SKIPPED
 * 
 * Cada trabajo registra su ejecución para la reconciliación al inicio.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyScheduledTasks {

    // Job 1: Reschedule orders (5:00 AM)
    public static final String JOB_RESCHEDULE_ORDERS = "RESCHEDULE_ORDERS";
    public static final String CRON_RESCHEDULE_ORDERS = "0 0 5 * * ?";
    public static final int RESCHEDULE_HOUR = 5;
    public static final int RESCHEDULE_MINUTE = 0;

    // Job 2: Auto-close routes (9:00 PM)
    public static final String JOB_AUTO_CLOSE_ROUTES = "AUTO_CLOSE_ROUTES";
    public static final String CRON_AUTO_CLOSE_ROUTES = "0 0 21 * * ?";
    public static final int AUTO_CLOSE_HOUR = 21;
    public static final int AUTO_CLOSE_MINUTE = 0;

    private final ManageOrdersUseCase manageOrdersUseCase;
    private final ExecuteRouteUseCase executeRouteUseCase;
    private final ScheduledJobService scheduledJobService;

    /**
     * Re-agenda automática de órdenes.
     * Se ejecuta a las 5:00 AM todos los días.
     * 
     * Las órdenes en estado FAILED (con < 3 intentos), SKIPPED, o RETURNED_TO_WAREHOUSE
     * vuelven a estado PENDING para ser incluidas en las rutas del día.
     */
    @Scheduled(cron = CRON_RESCHEDULE_ORDERS)
    public void rescheduleOrders() {
        executeRescheduleOrders();
    }

    /**
     * Cierre automático de rutas incompletas.
     * Se ejecuta a las 9:00 PM todos los días.
     * 
     * Las rutas en estado CONFIRMED o IN_PROGRESS se cierran automáticamente.
     * Las paradas PENDING pasan a SKIPPED y las órdenes asociadas a SKIPPED.
     */
    @Scheduled(cron = CRON_AUTO_CLOSE_ROUTES)
    public void autoCloseRoutes() {
        executeAutoCloseRoutes();
    }

    /**
     * Ejecuta el trabajo de re-agenda.
     * Puede ser llamado desde el scheduler o desde la reconciliación de inicio.
     * 
     * @return número de órdenes re-agendadas
     */
    public int executeRescheduleOrders() {
        log.info("=== INICIO: Re-agenda automática de órdenes ===");
        
        scheduledJobService.recordJobStart(JOB_RESCHEDULE_ORDERS, CRON_RESCHEDULE_ORDERS);
        
        try {
            int rescheduledCount = manageOrdersUseCase.rescheduleFailedOrders();
            
            scheduledJobService.recordJobSuccess(JOB_RESCHEDULE_ORDERS);
            log.info("=== FIN: {} órdenes re-agendadas para hoy ===", rescheduledCount);
            
            return rescheduledCount;
        } catch (Exception e) {
            scheduledJobService.recordJobFailure(JOB_RESCHEDULE_ORDERS, e.getMessage());
            log.error("Error en re-agenda automática de órdenes: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Ejecuta el trabajo de cierre automático de rutas.
     * Puede ser llamado desde el scheduler o desde la reconciliación de inicio.
     * 
     * @return número de rutas cerradas
     */
    public int executeAutoCloseRoutes() {
        log.info("=== INICIO: Cierre automático de rutas incompletas ===");
        
        scheduledJobService.recordJobStart(JOB_AUTO_CLOSE_ROUTES, CRON_AUTO_CLOSE_ROUTES);
        
        try {
            int closedCount = executeRouteUseCase.autoCloseIncompleteRoutes();
            
            scheduledJobService.recordJobSuccess(JOB_AUTO_CLOSE_ROUTES);
            log.info("=== FIN: {} rutas cerradas automáticamente ===", closedCount);
            
            return closedCount;
        } catch (Exception e) {
            scheduledJobService.recordJobFailure(JOB_AUTO_CLOSE_ROUTES, e.getMessage());
            log.error("Error en cierre automático de rutas: {}", e.getMessage(), e);
            throw e;
        }
    }
}
