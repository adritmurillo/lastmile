package com.lastmile.domain.model;

public enum StopStatus {
    PENDING,    // Pendiente de entrega
    DELIVERED,  // Entregado exitosamente
    FAILED,     // Intento fallido (cuenta como intento)
    SKIPPED     // Ruta cerrada, courier no llegó a esta parada (NO cuenta como intento)
}
