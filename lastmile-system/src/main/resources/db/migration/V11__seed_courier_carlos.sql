INSERT INTO vehicles (id, license_plate, type, max_weight_kg, max_volume_cm3, status)
VALUES ('182ad390-7c13-4e3c-b697-7049d8d44f6b', 'FFF-982', 'MOTORCYCLE', 50, 20000, 'AVAILABLE');

INSERT INTO couriers (id, first_name, last_name, document_number, phone, status, vehicle_id, created_at)
VALUES ('e8c6d367-abb7-4dab-88b7-efb4a008341c', 'Carlos', 'Ramirez', '12345678', '+51987654321', 'ACTIVE', '182ad390-7c13-4e3c-b697-7049d8d44f6b', NOW());