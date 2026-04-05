package com.lastmile.domain.port.in;

import com.lastmile.domain.model.RouteCloseReason;
import com.lastmile.domain.model.RouteCloseRequest;

import java.util.List;
import java.util.UUID;

public interface ManageRouteCloseRequestUseCase {
    
    /**
     * Courier solicita cierre anticipado de su ruta.
     * Crea una solicitud pendiente que debe ser aprobada por un dispatcher.
     *
     * @param routeId ID de la ruta a cerrar
     * @param courierId ID del courier que solicita
     * @param reason Motivo del cierre (enum)
     * @param message Mensaje descriptivo del courier (obligatorio)
     * @param photoUrl URL de foto de evidencia (opcional)
     * @return La solicitud de cierre creada
     */
    RouteCloseRequest requestClose(UUID routeId, UUID courierId, RouteCloseReason reason, 
                                   String message, String photoUrl);
    
    /**
     * Obtiene solicitudes pendientes para mostrar al dispatcher.
     *
     * @return Lista de solicitudes pendientes con datos del courier y ruta
     */
    List<RouteCloseRequest> getPendingRequests();
    
    /**
     * Dispatcher aprueba una solicitud de cierre.
     * - Cambia status de solicitud a APPROVED
     * - Cambia route status a CLOSED
     * - Cambia stops pendientes a RETURNED
     * - Cambia orders IN_TRANSIT/PICKED_UP a RETURNED_TO_WAREHOUSE
     * - Crea registros de OrderReturn para excluir courier de esos paquetes hoy
     * - Verifica si el courier debe ser suspendido (3 cierres en 7 días)
     *
     * @param requestId ID de la solicitud
     * @param dispatcherId ID del dispatcher que aprueba
     * @return La solicitud actualizada
     */
    RouteCloseRequest approveRequest(UUID requestId, UUID dispatcherId);
    
    /**
     * Dispatcher rechaza una solicitud de cierre.
     * El courier debe continuar con la ruta.
     *
     * @param requestId ID de la solicitud
     * @param dispatcherId ID del dispatcher que rechaza
     * @return La solicitud actualizada
     */
    RouteCloseRequest rejectRequest(UUID requestId, UUID dispatcherId);
    
    /**
     * Obtiene una solicitud por ID.
     *
     * @param requestId ID de la solicitud
     * @return La solicitud
     */
    RouteCloseRequest getById(UUID requestId);
    
    /**
     * Obtiene la solicitud pendiente de una ruta (si existe).
     *
     * @param routeId ID de la ruta
     * @return La solicitud pendiente o null
     */
    RouteCloseRequest getPendingRequestForRoute(UUID routeId);
}
