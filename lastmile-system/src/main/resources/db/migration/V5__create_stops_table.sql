CREATE TABLE stops (
                       id                  UUID        PRIMARY KEY,
                       route_id            UUID        NOT NULL REFERENCES routes(id),
                       order_id            UUID        NOT NULL REFERENCES orders(id),
                       stop_order          INT         NOT NULL,
                       estimated_arrival   TIMESTAMP,
                       actual_arrival      TIMESTAMP,
                       status              VARCHAR(20) NOT NULL,
                       failure_reason      VARCHAR(50),
                       proof_photo_url     TEXT
);

CREATE INDEX idx_stops_route_id
    ON stops(route_id);

CREATE INDEX idx_stops_order_id
    ON stops(order_id);