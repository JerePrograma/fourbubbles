CREATE TABLE order_treatment_profiles (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    reception_id UUID NOT NULL REFERENCES order_receptions(id),
    color_group VARCHAR(30) NOT NULL,
    material_group VARCHAR(30) NOT NULL,
    max_temperature_c INTEGER NOT NULL,
    dryer_allowed BOOLEAN NOT NULL,
    fragrance_policy VARCHAR(30) NOT NULL,
    softener_allowed BOOLEAN NOT NULL,
    hypoallergenic BOOLEAN NOT NULL,
    baby_clothes BOOLEAN NOT NULL,
    pet_contact BOOLEAN NOT NULL,
    heavy_soil BOOLEAN NOT NULL,
    exclusive_cycle BOOLEAN NOT NULL,
    notes VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_order_treatment_profiles_order UNIQUE (order_id),
    CONSTRAINT uk_order_treatment_profiles_reception UNIQUE (reception_id),
    CONSTRAINT ck_treatment_color_group CHECK (color_group IN ('WHITES','LIGHT','DARK','MIXED','UNKNOWN')),
    CONSTRAINT ck_treatment_material_group CHECK (material_group IN ('COTTON','SYNTHETIC','DELICATE','WOOL','MIXED')),
    CONSTRAINT ck_treatment_temperature CHECK (max_temperature_c BETWEEN 20 AND 95),
    CONSTRAINT ck_treatment_fragrance CHECK (fragrance_policy IN ('NONE','STANDARD','CUSTOM'))
);

CREATE INDEX ix_treatment_profiles_compatibility ON order_treatment_profiles
    (color_group, material_group, max_temperature_c, exclusive_cycle);

CREATE TABLE compatibility_evaluations (
    id UUID PRIMARY KEY,
    order_a_id UUID NOT NULL REFERENCES laundry_orders(id),
    order_b_id UUID NOT NULL REFERENCES laundry_orders(id),
    profile_a_version BIGINT NOT NULL,
    profile_b_version BIGINT NOT NULL,
    rule_version VARCHAR(40) NOT NULL,
    compatible BOOLEAN NOT NULL,
    reasons JSONB NOT NULL,
    recommendation JSONB NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_compatibility_distinct_orders CHECK (order_a_id <> order_b_id),
    CONSTRAINT ck_compatibility_ordering CHECK (order_a_id::text < order_b_id::text),
    CONSTRAINT uk_compatibility_profile_versions UNIQUE
        (order_a_id, order_b_id, profile_a_version, profile_b_version, rule_version)
);

CREATE INDEX ix_compatibility_evaluations_orders ON compatibility_evaluations
    (order_a_id, order_b_id, created_at DESC);

CREATE TABLE compatibility_exceptions (
    id UUID PRIMARY KEY,
    evaluation_id UUID NOT NULL REFERENCES compatibility_evaluations(id),
    reason VARCHAR(1000) NOT NULL,
    authorized_by VARCHAR(100) NOT NULL,
    authorized_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_compatibility_exceptions_evaluation UNIQUE (evaluation_id)
);
