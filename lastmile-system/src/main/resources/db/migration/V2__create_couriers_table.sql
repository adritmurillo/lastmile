CREATE TABLE couriers (
                          id              UUID            PRIMARY KEY,
                          first_name      VARCHAR(100)    NOT NULL,
                          last_name       VARCHAR(100)    NOT NULL,
                          document_number VARCHAR(20)     NOT NULL UNIQUE,
                          phone           VARCHAR(20)     NOT NULL,
                          status          VARCHAR(30)     NOT NULL,
                          vehicle_id      UUID            REFERENCES vehicles(id),
                          created_at      TIMESTAMP       NOT NULL
);
