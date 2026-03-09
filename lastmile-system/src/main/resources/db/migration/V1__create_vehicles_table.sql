CREATE TABLE vehicles (
                          id              UUID        PRIMARY KEY,
                          license_plate   VARCHAR(20) NOT NULL UNIQUE,
                          type            VARCHAR(20) NOT NULL,
                          max_weight_kg   FLOAT       NOT NULL,
                          max_volume_cm3  FLOAT       NOT NULL,
                          status          VARCHAR(30) NOT NULL
);