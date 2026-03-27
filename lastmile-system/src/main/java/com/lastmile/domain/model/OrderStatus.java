package com.lastmile.domain.model;

public enum OrderStatus {
    PENDING,                // Orden creada, esperando llegada física al almacén
    READY_TO_DISPATCH,      // Paquete recibido en almacén, listo para asignar
    ASSIGNED,               // Asignado a una ruta/courier
    PICKED_UP,              // Courier confirmó recogida del paquete (escaneó QR)
    IN_TRANSIT,             // Courier inició la ruta, en camino
    DELIVERED,              // Entregado exitosamente
    FAILED,                 // Intento de entrega fallido (cuenta como intento)
    SKIPPED,                // Ruta cerrada, courier no llegó a esta parada (NO cuenta como intento)
    RETURNED_TO_WAREHOUSE,  // Devuelto al almacén por cierre manual de ruta
    RETURNED,               // Devuelto definitivamente (3 intentos fallidos)
    CANCELLED,              // Cancelado por admin/dispatcher
}
