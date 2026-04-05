package com.lastmile.domain.model;

public enum CourierStatus {
    ACTIVE,         // Activo y disponible
    INACTIVE,       // Inactivo (no disponible)
    ON_VACATION,    // De vacaciones
    SUSPENDED       // Suspendido por cierres recurrentes (requiere revisión admin)
}
