-- =====================================================
-- SCRIPT DE PREPARACIÓN PARA PRUEBAS - LASTMILE
-- Ejecutar en: docker exec -it lastmile-postgres psql -U lastmile_user -d lastmile_db
-- =====================================================

-- 1. LIMPIAR DATOS EXISTENTES (en orden correcto por FK)
TRUNCATE TABLE stop_photos CASCADE;
TRUNCATE TABLE stops CASCADE;
TRUNCATE TABLE routes CASCADE;
TRUNCATE TABLE orders CASCADE;
-- NO limpiar: users, couriers, vehicles (datos maestros)

-- 2. VERIFICAR COURIERS EXISTENTES
SELECT id, full_name, status FROM couriers;

-- 3. VERIFICAR USUARIOS EXISTENTES
SELECT id, username, role FROM users;

-- 4. SI NO HAY COURIERS, CREAR DATOS DE PRUEBA
-- (Solo ejecutar si la consulta anterior está vacía)

/*
-- Crear vehículos de prueba
INSERT INTO vehicles (id, license_plate, type, max_weight_kg, max_volume_cm3, status)
VALUES 
    ('11111111-1111-1111-1111-111111111111', 'ABC-123', 'MOTORCYCLE', 50, 100000, 'AVAILABLE'),
    ('22222222-2222-2222-2222-222222222222', 'DEF-456', 'VAN', 500, 2000000, 'AVAILABLE'),
    ('33333333-3333-3333-3333-333333333333', 'GHI-789', 'MOTORCYCLE', 50, 100000, 'AVAILABLE');

-- Crear couriers de prueba
INSERT INTO couriers (id, first_name, last_name, document_number, phone, status, vehicle_id)
VALUES
    ('aaaa1111-1111-1111-1111-111111111111', 'María', 'García', '12345678', '+51999111111', 'ACTIVE', '11111111-1111-1111-1111-111111111111'),
    ('bbbb2222-2222-2222-2222-222222222222', 'Carlos', 'López', '87654321', '+51999222222', 'ACTIVE', '22222222-2222-2222-2222-222222222222'),
    ('cccc3333-3333-3333-3333-333333333333', 'Ana', 'Martínez', '11223344', '+51999333333', 'ACTIVE', '33333333-3333-3333-3333-333333333333');

-- Crear usuarios courier
INSERT INTO users (id, username, password_hash, email, role, courier_id)
VALUES
    ('uuuu1111-1111-1111-1111-111111111111', 'maria', '$2a$10$...', 'maria@test.com', 'COURIER', 'aaaa1111-1111-1111-1111-111111111111'),
    ('uuuu2222-2222-2222-2222-222222222222', 'carlos', '$2a$10$...', 'carlos@test.com', 'COURIER', 'bbbb2222-2222-2222-2222-222222222222'),
    ('uuuu3333-3333-3333-3333-333333333333', 'ana', '$2a$10$...', 'ana@test.com', 'COURIER', 'cccc3333-3333-3333-3333-333333333333');
*/

-- 5. VER ESTADO ACTUAL
SELECT 'ÓRDENES:' as tabla, status, COUNT(*) as cantidad FROM orders GROUP BY status
UNION ALL
SELECT 'RUTAS:', status::text, COUNT(*) FROM routes GROUP BY status
UNION ALL
SELECT 'STOPS:', status::text, COUNT(*) FROM stops GROUP BY status;
