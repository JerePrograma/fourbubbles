CREATE TABLE zones (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    locality VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_zones_code UNIQUE (code)
);

CREATE TABLE clients (
    id UUID PRIMARY KEY,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    whatsapp VARCHAR(30) NOT NULL,
    email VARCHAR(160),
    status VARCHAR(30) NOT NULL,
    acquisition_source VARCHAR(100),
    preferences_json JSONB NOT NULL DEFAULT '{}'::jsonb,
    notes VARCHAR(2000),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_clients_status CHECK (status IN ('ACTIVE', 'SUSPENDED', 'INACTIVE'))
);

CREATE UNIQUE INDEX uk_clients_active_whatsapp ON clients (whatsapp) WHERE deleted_at IS NULL;
CREATE INDEX ix_clients_name ON clients (last_name, first_name) WHERE deleted_at IS NULL;

CREATE TABLE addresses (
    id UUID PRIMARY KEY,
    client_id UUID NOT NULL REFERENCES clients(id),
    zone_id UUID NOT NULL REFERENCES zones(id),
    street VARCHAR(160) NOT NULL,
    number VARCHAR(20) NOT NULL,
    extra VARCHAR(120),
    locality VARCHAR(120) NOT NULL,
    neighborhood VARCHAR(120),
    delivery_references VARCHAR(500),
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_addresses_one_primary_per_client ON addresses (client_id) WHERE is_primary = TRUE AND active = TRUE;
CREATE INDEX ix_addresses_zone ON addresses (zone_id, active);
