-- Tabla para rastrear la última ejecución de trabajos programados
-- Permite detectar trabajos perdidos cuando el servidor no estaba corriendo

CREATE TABLE scheduled_job_runs (
    id UUID PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL UNIQUE,
    cron_expression VARCHAR(50) NOT NULL,
    last_run_at TIMESTAMP NOT NULL,
    last_status VARCHAR(20) NOT NULL, -- SUCCESS, FAILED, RUNNING
    run_count INT DEFAULT 0,
    last_error_message TEXT,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);

-- Índice para búsquedas por nombre de trabajo
CREATE INDEX idx_scheduled_job_runs_job_name ON scheduled_job_runs(job_name);

COMMENT ON TABLE scheduled_job_runs IS 'Rastrea ejecuciones de trabajos programados para reconciliación al inicio';
COMMENT ON COLUMN scheduled_job_runs.job_name IS 'Nombre único del trabajo programado';
COMMENT ON COLUMN scheduled_job_runs.cron_expression IS 'Expresión cron del trabajo';
COMMENT ON COLUMN scheduled_job_runs.last_run_at IS 'Timestamp de la última ejecución';
COMMENT ON COLUMN scheduled_job_runs.last_status IS 'Estado de la última ejecución: SUCCESS, FAILED, RUNNING';
COMMENT ON COLUMN scheduled_job_runs.run_count IS 'Contador total de ejecuciones';
