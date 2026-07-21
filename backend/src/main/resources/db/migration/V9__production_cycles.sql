CREATE SEQUENCE production_cycle_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE production_machines (
    id UUID PRIMARY KEY,
    code VARCHAR(40) NOT NULL,
    name VARCHAR(120) NOT NULL,
    machine_type VARCHAR(20) NOT NULL,
    capacity_grams INTEGER NOT NULL,
    status VARCHAR(30) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_production_machines_code UNIQUE (code),
    CONSTRAINT ck_production_machine_type CHECK (machine_type IN ('WASHER','DRYER')),
    CONSTRAINT ck_production_machine_capacity CHECK (capacity_grams > 0),
    CONSTRAINT ck_production_machine_status CHECK (status IN ('ACTIVE','MAINTENANCE','OUT_OF_SERVICE'))
);

CREATE TABLE production_programs (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(140) NOT NULL,
    stage VARCHAR(20) NOT NULL,
    required_machine_type VARCHAR(20) NOT NULL,
    duration_minutes INTEGER NOT NULL,
    max_temperature_c INTEGER,
    gentle BOOLEAN NOT NULL DEFAULT FALSE,
    uses_softener BOOLEAN NOT NULL DEFAULT FALSE,
    fragrance_policy VARCHAR(30),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_production_programs_code UNIQUE (code),
    CONSTRAINT ck_production_program_stage CHECK (stage IN ('WASH','DRY')),
    CONSTRAINT ck_production_program_machine_type CHECK (required_machine_type IN ('WASHER','DRYER')),
    CONSTRAINT ck_production_program_duration CHECK (duration_minutes > 0),
    CONSTRAINT ck_production_program_temperature CHECK (max_temperature_c IS NULL OR max_temperature_c BETWEEN 20 AND 95),
    CONSTRAINT ck_production_program_fragrance CHECK (fragrance_policy IS NULL OR fragrance_policy IN ('NONE','STANDARD','CUSTOM')),
    CONSTRAINT ck_production_program_stage_machine CHECK (
        (stage = 'WASH' AND required_machine_type = 'WASHER' AND max_temperature_c IS NOT NULL AND fragrance_policy IS NOT NULL)
        OR
        (stage = 'DRY' AND required_machine_type = 'DRYER' AND max_temperature_c IS NULL AND fragrance_policy IS NULL AND uses_softener = FALSE)
    )
);

CREATE TABLE production_cycles (
    id UUID PRIMARY KEY,
    cycle_number VARCHAR(24) NOT NULL,
    idempotency_key VARCHAR(120) NOT NULL,
    machine_id UUID NOT NULL REFERENCES production_machines(id),
    program_id UUID NOT NULL REFERENCES production_programs(id),
    status VARCHAR(20) NOT NULL,
    planned_weight_grams INTEGER NOT NULL,
    actual_weight_grams INTEGER,
    notes VARCHAR(1500),
    started_at TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    cancelled_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_production_cycles_number UNIQUE (cycle_number),
    CONSTRAINT uk_production_cycles_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_production_cycle_status CHECK (status IN ('PLANNED','RUNNING','COMPLETED','CANCELLED')),
    CONSTRAINT ck_production_cycle_planned_weight CHECK (planned_weight_grams > 0),
    CONSTRAINT ck_production_cycle_actual_weight CHECK (actual_weight_grams IS NULL OR actual_weight_grams > 0),
    CONSTRAINT ck_production_cycle_dates CHECK (
        (status = 'PLANNED' AND started_at IS NULL AND completed_at IS NULL AND cancelled_at IS NULL)
        OR (status = 'RUNNING' AND started_at IS NOT NULL AND completed_at IS NULL AND cancelled_at IS NULL)
        OR (status = 'COMPLETED' AND started_at IS NOT NULL AND completed_at IS NOT NULL AND cancelled_at IS NULL)
        OR (status = 'CANCELLED' AND completed_at IS NULL AND cancelled_at IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uk_production_active_cycle_machine
    ON production_cycles(machine_id)
    WHERE status IN ('PLANNED','RUNNING');

CREATE INDEX ix_production_cycles_status_created
    ON production_cycles(status, created_at DESC);

CREATE TABLE production_cycle_orders (
    id UUID PRIMARY KEY,
    cycle_id UUID NOT NULL REFERENCES production_cycles(id) ON DELETE CASCADE,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    assignment_order INTEGER NOT NULL,
    assigned_weight_grams INTEGER NOT NULL,
    separation_required BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_production_cycle_order UNIQUE (cycle_id, order_id),
    CONSTRAINT uk_production_cycle_position UNIQUE (cycle_id, assignment_order),
    CONSTRAINT ck_production_cycle_position CHECK (assignment_order IN (1,2)),
    CONSTRAINT ck_production_assignment_weight CHECK (assigned_weight_grams > 0)
);

CREATE INDEX ix_production_cycle_orders_order ON production_cycle_orders(order_id);

CREATE TABLE production_cycle_history (
    id UUID PRIMARY KEY,
    cycle_id UUID NOT NULL REFERENCES production_cycles(id) ON DELETE CASCADE,
    previous_status VARCHAR(20),
    new_status VARCHAR(20) NOT NULL,
    observation VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_production_cycle_history_previous CHECK (previous_status IS NULL OR previous_status IN ('PLANNED','RUNNING','COMPLETED','CANCELLED')),
    CONSTRAINT ck_production_cycle_history_new CHECK (new_status IN ('PLANNED','RUNNING','COMPLETED','CANCELLED'))
);

INSERT INTO production_machines (
    id, code, name, machine_type, capacity_grams, status, active, notes,
    created_at, updated_at, created_by, updated_by, version
) VALUES
('94000000-0000-0000-0000-000000000001', 'WASHER_01', 'Lavadora principal', 'WASHER', 10000, 'ACTIVE', TRUE, 'Seed operativo inicial', NOW(), NOW(), 'flyway', 'flyway', 0),
('94000000-0000-0000-0000-000000000002', 'DRYER_01', 'Secadora principal', 'DRYER', 10000, 'ACTIVE', TRUE, 'Seed operativo inicial', NOW(), NOW(), 'flyway', 'flyway', 0);

INSERT INTO production_programs (
    id, code, name, stage, required_machine_type, duration_minutes,
    max_temperature_c, gentle, uses_softener, fragrance_policy, active, notes,
    created_at, updated_at, created_by, updated_by, version
) VALUES
('95000000-0000-0000-0000-000000000001', 'WASH_30_NONE', 'Lavado 30° sin fragancia', 'WASH', 'WASHER', 45, 30, FALSE, FALSE, 'NONE', TRUE, 'Carga sensible o hipoalergénica', NOW(), NOW(), 'flyway', 'flyway', 0),
('95000000-0000-0000-0000-000000000002', 'WASH_40_STANDARD', 'Lavado 40° estándar', 'WASH', 'WASHER', 55, 40, FALSE, TRUE, 'STANDARD', TRUE, 'Carga estándar compatible', NOW(), NOW(), 'flyway', 'flyway', 0),
('95000000-0000-0000-0000-000000000003', 'WASH_GENTLE_NONE', 'Lavado delicado 30°', 'WASH', 'WASHER', 50, 30, TRUE, FALSE, 'NONE', TRUE, 'Delicados, lana o recomendación GENTLE', NOW(), NOW(), 'flyway', 'flyway', 0),
('95000000-0000-0000-0000-000000000004', 'DRY_GENTLE', 'Secado delicado', 'DRY', 'DRYER', 45, NULL, TRUE, FALSE, NULL, TRUE, 'Secado suave', NOW(), NOW(), 'flyway', 'flyway', 0),
('95000000-0000-0000-0000-000000000005', 'DRY_NORMAL', 'Secado normal', 'DRY', 'DRYER', 50, NULL, FALSE, FALSE, NULL, TRUE, 'Secado estándar', NOW(), NOW(), 'flyway', 'flyway', 0);
