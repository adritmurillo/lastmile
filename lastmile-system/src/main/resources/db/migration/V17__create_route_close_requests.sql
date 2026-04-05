-- =====================================================
-- V17: Sistema de Cierre Anticipado de Rutas
-- =====================================================

-- Tabla de solicitudes de cierre de ruta
CREATE TABLE route_close_requests (
    id UUID PRIMARY KEY,
    route_id UUID NOT NULL REFERENCES routes(id),
    courier_id UUID NOT NULL REFERENCES couriers(id),
    reason VARCHAR(50) NOT NULL,
    message TEXT NOT NULL,
    photo_url TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Índices para búsquedas frecuentes
CREATE INDEX idx_close_requests_status ON route_close_requests(status);
CREATE INDEX idx_close_requests_courier ON route_close_requests(courier_id);
CREATE INDEX idx_close_requests_route ON route_close_requests(route_id);

-- Historial de devoluciones de paquetes (para exclusión de courier)
CREATE TABLE order_returns (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id),
    courier_id UUID NOT NULL REFERENCES couriers(id),
    route_id UUID NOT NULL REFERENCES routes(id),
    returned_at DATE NOT NULL DEFAULT CURRENT_DATE,
    reason VARCHAR(50)
);

-- Índice para verificar exclusiones del día (excluir courier que devolvió paquete hoy)
CREATE INDEX idx_order_returns_exclusion ON order_returns(order_id, courier_id, returned_at);
CREATE INDEX idx_order_returns_courier ON order_returns(courier_id, returned_at);
