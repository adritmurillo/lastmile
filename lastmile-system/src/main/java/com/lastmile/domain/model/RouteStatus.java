package com.lastmile.domain.model;

public enum RouteStatus {
    PENDING,        // Propuesta de ruta creada, sin confirmar
    CONFIRMED,      // Ruta confirmada por dispatcher, esperando courier
    IN_PROGRESS,    // Courier en la calle
    COMPLETED,      // Todas las paradas resueltas (DELIVERED o FAILED)
    CLOSED          // Cerrada antes de completar (manual o automático 9PM)
}
