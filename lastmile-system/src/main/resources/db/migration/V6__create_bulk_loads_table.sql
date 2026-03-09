CREATE TABLE bulk_loads (
                            id                  UUID        PRIMARY KEY,
                            file_name           VARCHAR(255) NOT NULL,
                            load_source         VARCHAR(20)  NOT NULL,
                            status              VARCHAR(30)  NOT NULL,
                            total_records       INT          NOT NULL DEFAULT 0,
                            successful_records  INT          NOT NULL DEFAULT 0,
                            failed_records      INT          NOT NULL DEFAULT 0,
                            started_at          TIMESTAMP    NOT NULL,
                            finished_at         TIMESTAMP,
                            uploaded_by         VARCHAR(100) NOT NULL
);

CREATE TABLE bulk_load_errors (
                                  id                  UUID        PRIMARY KEY,
                                  bulk_load_id        UUID        NOT NULL REFERENCES bulk_loads(id),
                                  row_number          INT         NOT NULL,
                                  error_description   TEXT        NOT NULL,
                                  problematic_value   TEXT
);

CREATE INDEX idx_bulk_load_errors_bulk_load_id
    ON bulk_load_errors(bulk_load_id);
