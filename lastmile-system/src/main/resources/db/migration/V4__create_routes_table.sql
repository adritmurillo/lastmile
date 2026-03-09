CREATE TABLE routes (
                        id                  UUID        PRIMARY KEY,
                        courier_id          UUID        NOT NULL REFERENCES couriers(id),
                        date                DATE        NOT NULL,
                        status              VARCHAR(30) NOT NULL,
                        total_weight_kg     FLOAT,
                        total_volume_cm3    FLOAT,
                        started_at          TIMESTAMP,
                        completed_at        TIMESTAMP
);

CREATE INDEX idx_routes_date
    ON routes(date);

CREATE INDEX idx_routes_courier_date
    ON routes(courier_id, date);