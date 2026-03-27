package com.lastmile.domain.port.in;

import com.lastmile.domain.model.FailureReason;
import com.lastmile.domain.model.Order;
import com.lastmile.domain.model.Route;
import com.lastmile.domain.model.Stop;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ExecuteRouteUseCase {
    Route getMyRouteForToday(UUID courierId);
    Route startRoute(UUID routeId);
    Stop registerSuccessfulDelivery(UUID stopId, List<String> photoUrls);
    Stop registerFailedDelivery(UUID stopId, FailureReason reason, String failureNotes);
    Route completeRoute(UUID routeId);
    List<Route> getRoutesByDate(LocalDate date);
    List<Stop> getPendingStopsFromPreviousDays(UUID courierId);
    Route closeRoute(UUID routeId, String reason);
    List<Route> getCourierHistory(UUID courierId);

    /**
     * Escanea y confirma recogida de un paquete por el courier.
     * Cambia el estado de la orden de ASSIGNED a PICKED_UP.
     *
     * @param routeId ID de la ruta del courier
     * @param trackingCode Código de seguimiento escaneado del QR
     * @return La orden actualizada con estado PICKED_UP
     * @throws OrderNotFoundException si el trackingCode no existe
     * @throws IllegalStateException si el paquete no pertenece a esta ruta
     */
    Order scanPickup(UUID routeId, String trackingCode);

    /**
     * Obtiene el estado de recogida de paquetes para una ruta.
     *
     * @param routeId ID de la ruta
     * @return Información de cuántos paquetes faltan por escanear
     */
    PickupStatus getPickupStatus(UUID routeId);

    /**
     * Cierra automáticamente todas las rutas incompletas (CONFIRMED o IN_PROGRESS).
     * Las paradas PENDING pasan a SKIPPED y las órdenes asociadas pasan a SKIPPED.
     * Se ejecuta automáticamente a las 9 PM.
     *
     * @return número de rutas cerradas
     */
    int autoCloseIncompleteRoutes();

    record PickupStatus(int totalPackages, int scannedPackages, int pendingPackages, boolean readyToStart) {}
}
