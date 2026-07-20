CREATE SEQUENCE reception_label_seq START WITH 1 INCREMENT BY 1;

ALTER TABLE laundry_orders
    ADD COLUMN actual_physical_pieces INTEGER;

ALTER TABLE laundry_orders
    ADD CONSTRAINT ck_laundry_orders_actual_physical_pieces
        CHECK (actual_physical_pieces IS NULL OR actual_physical_pieces > 0);

CREATE TABLE order_receptions (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES laundry_orders(id),
    idempotency_key VARCHAR(120) NOT NULL,
    received_at TIMESTAMPTZ NOT NULL,
    declared_physical_pieces INTEGER NOT NULL,
    actual_physical_pieces INTEGER NOT NULL,
    declared_weight_grams INTEGER,
    actual_weight_grams INTEGER NOT NULL,
    piece_difference INTEGER NOT NULL,
    weight_difference_grams INTEGER,
    condition_notes VARCHAR(2000),
    damage_detected BOOLEAN NOT NULL DEFAULT FALSE,
    stain_detected BOOLEAN NOT NULL DEFAULT FALSE,
    requires_customer_approval BOOLEAN NOT NULL DEFAULT FALSE,
    approval_status VARCHAR(30) NOT NULL,
    approval_at TIMESTAMPTZ,
    approval_by VARCHAR(100),
    approval_notes VARCHAR(1000),
    label_code VARCHAR(30) NOT NULL,
    bag_code VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_order_receptions_order UNIQUE (order_id),
    CONSTRAINT uk_order_receptions_idempotency UNIQUE (idempotency_key),
    CONSTRAINT uk_order_receptions_label UNIQUE (label_code),
    CONSTRAINT ck_order_receptions_declared_pieces CHECK (declared_physical_pieces >= 0),
    CONSTRAINT ck_order_receptions_actual_pieces CHECK (actual_physical_pieces > 0),
    CONSTRAINT ck_order_receptions_declared_weight CHECK (declared_weight_grams IS NULL OR declared_weight_grams > 0),
    CONSTRAINT ck_order_receptions_actual_weight CHECK (actual_weight_grams > 0),
    CONSTRAINT ck_order_receptions_approval_status CHECK (
        approval_status IN ('NOT_REQUIRED', 'PENDING', 'APPROVED', 'REJECTED')
    ),
    CONSTRAINT ck_order_receptions_approval_consistency CHECK (
        (approval_status IN ('NOT_REQUIRED', 'PENDING') AND approval_at IS NULL AND approval_by IS NULL)
        OR
        (approval_status IN ('APPROVED', 'REJECTED') AND approval_at IS NOT NULL AND approval_by IS NOT NULL)
    ),
    CONSTRAINT ck_order_receptions_requirement_consistency CHECK (
        (requires_customer_approval = FALSE AND approval_status = 'NOT_REQUIRED')
        OR
        (requires_customer_approval = TRUE AND approval_status IN ('PENDING', 'APPROVED', 'REJECTED'))
    )
);

CREATE INDEX ix_order_receptions_received_at ON order_receptions (received_at DESC);
CREATE INDEX ix_order_receptions_approval ON order_receptions (approval_status, received_at DESC);

CREATE TABLE reception_items (
    id UUID PRIMARY KEY,
    reception_id UUID NOT NULL REFERENCES order_receptions(id) ON DELETE CASCADE,
    equivalence_id UUID REFERENCES garment_equivalences(id),
    equivalence_code_snapshot VARCHAR(60) NOT NULL,
    equivalence_name_snapshot VARCHAR(160) NOT NULL,
    declared_physical_pieces INTEGER NOT NULL,
    actual_physical_pieces INTEGER NOT NULL,
    piece_difference INTEGER NOT NULL,
    damage_detected BOOLEAN NOT NULL DEFAULT FALSE,
    stain_detected BOOLEAN NOT NULL DEFAULT FALSE,
    observations VARCHAR(1000),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_reception_items_code UNIQUE (reception_id, equivalence_code_snapshot),
    CONSTRAINT ck_reception_items_declared CHECK (declared_physical_pieces >= 0),
    CONSTRAINT ck_reception_items_actual CHECK (actual_physical_pieces >= 0)
);

CREATE INDEX ix_reception_items_reception ON reception_items (reception_id);

CREATE TABLE reception_evidences (
    id UUID PRIMARY KEY,
    reception_id UUID NOT NULL REFERENCES order_receptions(id) ON DELETE CASCADE,
    object_key VARCHAR(500) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    content_type VARCHAR(120) NOT NULL,
    size_bytes BIGINT NOT NULL,
    sha256 VARCHAR(64) NOT NULL,
    caption VARCHAR(500),
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    created_by VARCHAR(100) NOT NULL,
    updated_by VARCHAR(100) NOT NULL,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_reception_evidences_object_key UNIQUE (object_key),
    CONSTRAINT ck_reception_evidences_size CHECK (size_bytes > 0),
    CONSTRAINT ck_reception_evidences_sha256 CHECK (sha256 ~ '^[0-9A-Fa-f]{64}$')
);

CREATE INDEX ix_reception_evidences_reception ON reception_evidences (reception_id);
