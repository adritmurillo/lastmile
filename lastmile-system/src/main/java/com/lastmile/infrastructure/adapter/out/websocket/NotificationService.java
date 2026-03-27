package com.lastmile.infrastructure.adapter.out.websocket;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * Servicio para enviar notificaciones en tiempo real a través de WebSocket.
 * 
 * Topics disponibles:
 * - /topic/dispatcher/close-requests: Notificaciones de solicitudes de cierre para dispatchers
 * - /topic/courier/{courierId}: Notificaciones específicas para un courier
 * - /topic/dispatcher/orders: Notificaciones de nuevas órdenes para dispatchers
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {
    
    private static final String TOPIC_DISPATCHER_CLOSE_REQUESTS = "/topic/dispatcher/close-requests";
    private static final String TOPIC_DISPATCHER_ORDERS = "/topic/dispatcher/orders";
    private static final String TOPIC_COURIER_PREFIX = "/topic/courier/";
    
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * Notifica a los dispatchers que un courier solicitó cierre de ruta.
     */
    public void notifyRouteCloseRequested(UUID requestId, UUID routeId, UUID courierId, 
                                          String courierName, String reason, int pendingStops) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.ROUTE_CLOSE_REQUESTED,
                "Solicitud de Cierre de Ruta",
                String.format("%s solicita cerrar su ruta. Motivo: %s. Paradas pendientes: %d", 
                        courierName, reason, pendingStops),
                Map.of(
                        "requestId", requestId.toString(),
                        "routeId", routeId.toString(),
                        "courierId", courierId.toString(),
                        "courierName", courierName,
                        "reason", reason,
                        "pendingStops", pendingStops
                )
        );
        
        sendToDispatchers(TOPIC_DISPATCHER_CLOSE_REQUESTS, message);
    }
    
    /**
     * Notifica a un courier que su solicitud de cierre fue aprobada.
     */
    public void notifyCloseRequestApproved(UUID courierId, UUID routeId, int returnedPackages) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.ROUTE_CLOSE_APPROVED,
                "Cierre de Ruta Aprobado",
                String.format("Tu solicitud de cierre fue aprobada. %d paquetes marcados para devolución.", 
                        returnedPackages),
                Map.of(
                        "routeId", routeId.toString(),
                        "returnedPackages", returnedPackages
                )
        );
        
        sendToCourier(courierId, message);
    }
    
    /**
     * Notifica a un courier que su solicitud de cierre fue rechazada.
     */
    public void notifyCloseRequestRejected(UUID courierId, UUID routeId) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.ROUTE_CLOSE_REJECTED,
                "Cierre de Ruta Rechazado",
                "Tu solicitud de cierre fue rechazada. Debes continuar con la ruta.",
                Map.of("routeId", routeId.toString())
        );
        
        sendToCourier(courierId, message);
    }
    
    /**
     * Notifica a los dispatchers que se creó una nueva orden (desde webhook).
     */
    public void notifyNewOrderCreated(UUID orderId, String trackingCode, String source) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.NEW_ORDER_CREATED,
                "Nueva Orden Recibida",
                String.format("Nueva orden %s creada desde %s", trackingCode, source),
                Map.of(
                        "orderId", orderId.toString(),
                        "trackingCode", trackingCode,
                        "source", source
                )
        );
        
        sendToDispatchers(TOPIC_DISPATCHER_ORDERS, message);
    }
    
    /**
     * Notifica a los dispatchers que un courier completó una entrega.
     */
    public void notifyOrderDelivered(UUID orderId, String trackingCode, UUID courierId, String courierName) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.ORDER_DELIVERED,
                "Entrega Completada",
                String.format("%s entregó orden %s", courierName, trackingCode),
                Map.of(
                        "orderId", orderId.toString(),
                        "trackingCode", trackingCode,
                        "courierId", courierId.toString(),
                        "courierName", courierName
                )
        );
        
        sendToDispatchers(TOPIC_DISPATCHER_ORDERS, message);
    }
    
    /**
     * Notifica a los dispatchers que una entrega falló.
     */
    public void notifyOrderFailed(UUID orderId, String trackingCode, UUID courierId, 
                                  String courierName, String failureReason) {
        NotificationMessage message = NotificationMessage.of(
                NotificationMessage.NotificationType.ORDER_FAILED,
                "Entrega Fallida",
                String.format("%s no pudo entregar %s: %s", courierName, trackingCode, failureReason),
                Map.of(
                        "orderId", orderId.toString(),
                        "trackingCode", trackingCode,
                        "courierId", courierId.toString(),
                        "courierName", courierName,
                        "failureReason", failureReason
                )
        );
        
        sendToDispatchers(TOPIC_DISPATCHER_ORDERS, message);
    }
    
    /**
     * Envía un mensaje a todos los dispatchers suscritos a un topic.
     */
    private void sendToDispatchers(String topic, NotificationMessage message) {
        try {
            messagingTemplate.convertAndSend(topic, message);
            log.debug("Notification sent to {}: type={}", topic, message.getType());
        } catch (Exception e) {
            log.error("Failed to send notification to {}: {}", topic, e.getMessage(), e);
        }
    }
    
    /**
     * Envía un mensaje a un courier específico.
     */
    private void sendToCourier(UUID courierId, NotificationMessage message) {
        String topic = TOPIC_COURIER_PREFIX + courierId.toString();
        try {
            messagingTemplate.convertAndSend(topic, message);
            log.debug("Notification sent to courier {}: type={}", courierId, message.getType());
        } catch (Exception e) {
            log.error("Failed to send notification to courier {}: {}", courierId, e.getMessage(), e);
        }
    }
}
