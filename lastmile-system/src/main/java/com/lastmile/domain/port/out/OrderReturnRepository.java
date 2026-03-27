package com.lastmile.domain.port.out;

import com.lastmile.domain.model.OrderReturn;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface OrderReturnRepository {
    OrderReturn save(OrderReturn orderReturn);
    
    /**
     * Verifica si un courier devolvió un paquete específico en una fecha dada.
     * Se usa para excluir couriers que ya devolvieron un paquete hoy.
     */
    boolean existsByOrderIdAndCourierIdAndReturnedAt(UUID orderId, UUID courierId, LocalDate date);
    
    /**
     * Obtiene la lista de courier IDs que devolvieron un paquete específico hoy.
     * Se usa al generar propuestas para excluir esos couriers.
     */
    List<UUID> findCourierIdsWhoReturnedOrderOnDate(UUID orderId, LocalDate date);
    
    /**
     * Guarda múltiples devoluciones en batch.
     */
    void saveAll(List<OrderReturn> orderReturns);
}
