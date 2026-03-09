CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username VARCHAR(100) NOT NULL UNIQUE,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
    );

INSERT INTO users (id, username, email, password, role, active, created_at) VALUES
                                                                                (gen_random_uuid(), 'admin', 'admin@lastmile.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFrm', 'ADMIN', TRUE, NOW()),
                                                                                (gen_random_uuid(), 'dispatcher', 'dispatcher@lastmile.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFrm', 'DISPATCHER', TRUE, NOW()),
                                                                                (gen_random_uuid(), 'carlos', 'carlos@lastmile.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lFrm', 'COURIER', TRUE, NOW());