CREATE TABLE service_offerings (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(140) NOT NULL,
    description VARCHAR(1000),
    max_equivalent_units NUMERIC(10,2),
    max_weight_grams INTEGER,
    safe_capacity_grams INTEGER,
    requires_quote BOOLEAN NOT NULL DEFAULT FALSE,
    valid_from DATE NOT NULL,
    valid_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_service_offerings_version UNIQUE (code, valid_from),
    CONSTRAINT ck_service_weights CHECK (
        (max_weight_grams IS NULL OR max_weight_grams > 0)
        AND (safe_capacity_grams IS NULL OR safe_capacity_grams > 0)
    ),
    CONSTRAINT ck_service_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE TABLE garment_equivalences (
    id UUID PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(140) NOT NULL,
    category VARCHAR(100) NOT NULL,
    physical_units_per_group INTEGER NOT NULL,
    equivalent_units NUMERIC(10,2) NOT NULL,
    estimated_weight_grams INTEGER,
    estimated_volume_units NUMERIC(10,2),
    common_wash_allowed BOOLEAN NOT NULL,
    dryer_allowed BOOLEAN NOT NULL,
    exclusive_cycle_required BOOLEAN NOT NULL,
    quote_required BOOLEAN NOT NULL,
    valid_from DATE NOT NULL,
    valid_to DATE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_garment_equivalences_version UNIQUE (code, valid_from),
    CONSTRAINT ck_equivalence_values CHECK (physical_units_per_group > 0 AND equivalent_units > 0),
    CONSTRAINT ck_equivalence_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE TABLE price_definitions (
    id UUID PRIMARY KEY,
    service_id UUID NOT NULL REFERENCES service_offerings(id),
    zone_id UUID REFERENCES zones(id),
    amount NUMERIC(15,2) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    customer_type VARCHAR(40),
    channel VARCHAR(40),
    modality VARCHAR(40),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    reason VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_price_amount CHECK (amount >= 0),
    CONSTRAINT ck_price_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX ix_price_lookup ON price_definitions (service_id, zone_id, valid_from DESC) WHERE active = TRUE;

CREATE TABLE promotions (
    id UUID PRIMARY KEY,
    code VARCHAR(60) NOT NULL,
    name VARCHAR(140) NOT NULL,
    description VARCHAR(1200),
    valid_from TIMESTAMPTZ NOT NULL,
    valid_to TIMESTAMPTZ,
    total_quota INTEGER,
    daily_quota INTEGER,
    monthly_quota INTEGER,
    new_customers_only BOOLEAN NOT NULL DEFAULT FALSE,
    automatic_applicable BOOLEAN NOT NULL DEFAULT FALSE,
    applicable_service_code VARCHAR(50),
    one_per_address BOOLEAN NOT NULL DEFAULT FALSE,
    stackable BOOLEAN NOT NULL DEFAULT FALSE,
    fixed_price NUMERIC(15,2),
    percentage_discount NUMERIC(7,4),
    credit_amount NUMERIC(15,2),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_promotions_version UNIQUE (code, valid_from),
    CONSTRAINT ck_promotion_validity CHECK (valid_to IS NULL OR valid_to >= valid_from),
    CONSTRAINT ck_promotion_quotas CHECK (
        (total_quota IS NULL OR total_quota >= 0)
        AND (daily_quota IS NULL OR daily_quota >= 0)
        AND (monthly_quota IS NULL OR monthly_quota >= 0)
    ),
    CONSTRAINT ck_promotion_discount CHECK (percentage_discount IS NULL OR (percentage_discount >= 0 AND percentage_discount <= 1)),
    CONSTRAINT ck_promotion_status CHECK (status IN ('DRAFT', 'ACTIVE', 'PAUSED', 'CANCELLED', 'EXPIRED'))
);

CREATE TABLE promotion_usages (
    id UUID PRIMARY KEY,
    promotion_id UUID NOT NULL REFERENCES promotions(id),
    client_id UUID NOT NULL REFERENCES clients(id),
    address_id UUID NOT NULL REFERENCES addresses(id),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_promotion_usages_promotion_date ON promotion_usages (promotion_id, created_at);
CREATE INDEX ix_promotion_usages_client ON promotion_usages (client_id, created_at);
CREATE INDEX ix_promotion_usages_address ON promotion_usages (address_id, created_at);
