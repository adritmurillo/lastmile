package com.lastmile.infrastructure.adapter.out.websocket;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Mensaje de notificación enviado a través de WebSocket.
 */
@Getter
@Builder
public class NotificationMessage {
    
    /**
     * Tipo de notificación para que el cliente pueda manejarla apropiadamente.
     */
    private final NotificationType type;
    
    /**
     * Título corto de la notificación.
     */
    private final String title;
    
    /**
     * Cuerpo/mensaje de la notificación.
     */
    private final String body;
    
    /**
     * Datos adicionales específicos del tipo de notificación.
     * Por ejemplo: requestId, routeId, courierId, etc.
     */
    private final Map<String, Object> data;
    
    /**
     * Timestamp de cuando se generó la notificación.
     */
    private final LocalDateTime timestamp;
    
    /**
     * Crea una notificación con timestamp actual.
     */
    public static NotificationMessage of(NotificationType type, String title, String body, Map<String, Object> data) {
        return NotificationMessage.builder()
                .type(type)
                .title(title)
                .body(body)
                .data(data)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    /**
     * Tipos de notificación soportados.
     */
    public enum NotificationType {
        // Route Close Requests
        ROUTE_CLOSE_REQUESTED,      // Courier solicitó cierre de ruta
        ROUTE_CLOSE_APPROVED,       // Dispatcher aprobó cierre
        ROUTE_CLOSE_REJECTED,       // Dispatcher rechazó cierre
        
        // Orders
        NEW_ORDER_CREATED,          // Nueva orden creada (webhook)
        ORDER_DELIVERED,            // Orden entregada
        ORDER_FAILED,               // Orden fallida
        
        // Routes
        ROUTE_STARTED,              // Courier inició ruta
        ROUTE_COMPLETED,            // Ruta completada
        
        // Courier
        COURIER_ARRIVED_AT_STOP,    // Courier llegó a parada
        
        // System
        SYSTEM_ALERT               // Alertas del sistema
    }
}
