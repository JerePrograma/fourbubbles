ALTER TABLE addresses
    ADD COLUMN valid_from TIMESTAMPTZ,
    ADD COLUMN valid_to TIMESTAMPTZ;

UPDATE addresses
SET valid_from = created_at
WHERE valid_from IS NULL;

ALTER TABLE addresses
    ALTER COLUMN valid_from SET NOT NULL;

ALTER TABLE addresses
    ADD CONSTRAINT ck_addresses_validity CHECK (valid_to IS NULL OR valid_to >= valid_from),
    ADD CONSTRAINT ck_addresses_active_validity CHECK (
        (active = TRUE AND valid_to IS NULL)
        OR (active = FALSE AND valid_to IS NOT NULL)
    );

CREATE INDEX ix_addresses_client_validity
    ON addresses (client_id, valid_from DESC, valid_to);

ALTER TABLE laundry_orders
    ADD COLUMN automatic_quoted_price NUMERIC(15,2),
    ADD COLUMN manual_quote_reason VARCHAR(1000),
    ADD COLUMN manual_quote_at TIMESTAMPTZ,
    ADD COLUMN manual_quote_by VARCHAR(100);

UPDATE laundry_orders
SET automatic_quoted_price = quoted_price
WHERE automatic_quoted_price IS NULL;

ALTER TABLE laundry_orders
    ALTER COLUMN automatic_quoted_price SET NOT NULL;

ALTER TABLE laundry_orders
    ADD CONSTRAINT ck_order_automatic_quote CHECK (automatic_quoted_price >= 0),
    ADD CONSTRAINT ck_order_manual_quote_complete CHECK (
        (manual_quote_at IS NULL AND manual_quote_by IS NULL AND manual_quote_reason IS NULL)
        OR (manual_quote_at IS NOT NULL AND manual_quote_by IS NOT NULL AND manual_quote_reason IS NOT NULL)
    );
