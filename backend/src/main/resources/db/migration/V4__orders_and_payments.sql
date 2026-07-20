CREATE SEQUENCE order_number_seq START WITH 1 INCREMENT BY 1 NO CYCLE;

CREATE TABLE laundry_orders (
    id UUID PRIMARY KEY,
    order_number VARCHAR(20) NOT NULL,
    client_id UUID NOT NULL REFERENCES clients(id),
    address_id UUID NOT NULL REFERENCES addresses(id),
    service_id UUID NOT NULL REFERENCES service_offerings(id),
    price_definition_id UUID NOT NULL REFERENCES price_definitions(id),
    promotion_id UUID REFERENCES promotions(id),
    status VARCHAR(40) NOT NULL,
    payment_status VARCHAR(30) NOT NULL,
    physical_pieces INTEGER NOT NULL,
    equivalent_units NUMERIC(10,2) NOT NULL,
    declared_weight_grams INTEGER,
    actual_weight_grams INTEGER,
    exclusive_cycle BOOLEAN NOT NULL DEFAULT FALSE,
    requires_quote BOOLEAN NOT NULL DEFAULT FALSE,
    limit_reached VARCHAR(30) NOT NULL,
    quoted_price NUMERIC(15,2) NOT NULL,
    confirmed_price NUMERIC(15,2),
    currency_code CHAR(3) NOT NULL,
    price_breakdown JSONB NOT NULL,
    pickup_scheduled_at TIMESTAMPTZ,
    promised_at TIMESTAMPTZ,
    notes VARCHAR(2000),
    deleted_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_laundry_orders_number UNIQUE (order_number),
    CONSTRAINT ck_order_counts CHECK (physical_pieces > 0 AND equivalent_units > 0),
    CONSTRAINT ck_order_weights CHECK (
        (declared_weight_grams IS NULL OR declared_weight_grams > 0)
        AND (actual_weight_grams IS NULL OR actual_weight_grams > 0)
    ),
    CONSTRAINT ck_order_prices CHECK (quoted_price >= 0 AND (confirmed_price IS NULL OR confirmed_price >= 0))
);

CREATE INDEX ix_orders_client_date ON laundry_orders (client_id, created_at DESC) WHERE deleted_at IS NULL;
CREATE INDEX ix_orders_status_schedule ON laundry_orders (status, pickup_scheduled_at) WHERE deleted_at IS NULL;

ALTER TABLE promotion_usages
    ADD COLUMN order_id UUID NOT NULL REFERENCES laundry_orders(id);
ALTER TABLE promotion_usages
    ADD CONSTRAINT uk_promotion_usages_order UNIQUE (order_id);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    equivalence_id UUID NOT NULL REFERENCES garment_equivalences(id),
    physical_pieces INTEGER NOT NULL,
    group_count INTEGER NOT NULL,
    equivalent_units_applied NUMERIC(10,2) NOT NULL,
    estimated_weight_grams INTEGER,
    observations VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_order_item_values CHECK (physical_pieces > 0 AND group_count > 0 AND equivalent_units_applied > 0)
);

CREATE INDEX ix_order_items_order ON order_items (order_id);

CREATE TABLE order_state_history (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    previous_status VARCHAR(40),
    new_status VARCHAR(40) NOT NULL,
    observation VARCHAR(1000),
    location VARCHAR(200),
    notification_reference VARCHAR(200),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX ix_order_state_history_order ON order_state_history (order_id, created_at);

CREATE TABLE payment_methods (
    id UUID PRIMARY KEY,
    code VARCHAR(50) NOT NULL,
    name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_payment_methods_code UNIQUE (code)
);

CREATE TABLE payments (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    client_id UUID NOT NULL REFERENCES clients(id),
    payment_method_id UUID NOT NULL REFERENCES payment_methods(id),
    amount NUMERIC(15,2) NOT NULL,
    currency_code CHAR(3) NOT NULL,
    paid_at TIMESTAMPTZ NOT NULL,
    reference VARCHAR(160),
    notes VARCHAR(500),
    status VARCHAR(30) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_payment_amount CHECK (amount > 0),
    CONSTRAINT ck_payment_status CHECK (status IN ('PENDING', 'PAID', 'CANCELLED', 'REFUNDED'))
);

CREATE INDEX ix_payments_order ON payments (order_id, paid_at);
CREATE INDEX ix_payments_client ON payments (client_id, paid_at DESC);
