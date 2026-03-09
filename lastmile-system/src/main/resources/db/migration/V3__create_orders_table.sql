CREATE TABLE orders (
                        id                      UUID            PRIMARY KEY,
                        tracking_code           VARCHAR(50)     NOT NULL UNIQUE,
                        external_tracking_code  VARCHAR(100)    UNIQUE,
                        platform_order_number   VARCHAR(100),
                        recipient_name          VARCHAR(200)    NOT NULL,
                        recipient_phone         VARCHAR(20)     NOT NULL,
                        address_text            TEXT            NOT NULL,
                        latitude                FLOAT,
                        longitude               FLOAT,
                        weight_kg               FLOAT           NOT NULL,
                        volume_cm3              FLOAT           NOT NULL,
                        priority                VARCHAR(20)     NOT NULL,
                        status                  VARCHAR(20)     NOT NULL,
                        delivery_attempts       INT             NOT NULL DEFAULT 0,
                        delivery_deadline       DATE,
                        created_at              TIMESTAMP       NOT NULL,
                        notes                   TEXT,
                        load_source             VARCHAR(20)     NOT NULL
);

CREATE INDEX idx_orders_status
    ON orders(status);

CREATE INDEX idx_orders_tracking_code
    ON orders(tracking_code);

CREATE INDEX idx_orders_external_tracking
    ON orders(external_tracking_code);

CREATE INDEX idx_orders_delivery_deadline
    ON orders(delivery_deadline);