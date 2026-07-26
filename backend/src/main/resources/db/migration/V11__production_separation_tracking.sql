ALTER TABLE production_cycle_orders
    ADD COLUMN separation_container_code VARCHAR(80),
    ADD COLUMN separation_confirmed_at TIMESTAMPTZ,
    ADD COLUMN separation_confirmed_by VARCHAR(100);

ALTER TABLE production_cycle_orders
    ADD CONSTRAINT ck_production_separation_confirmation CHECK (
        (separation_container_code IS NULL
            AND separation_confirmed_at IS NULL
            AND separation_confirmed_by IS NULL)
        OR
        (separation_required = TRUE
            AND separation_container_code IS NOT NULL
            AND separation_confirmed_at IS NOT NULL
            AND separation_confirmed_by IS NOT NULL)
    );

CREATE UNIQUE INDEX uk_production_cycle_separation_container
    ON production_cycle_orders (cycle_id, UPPER(separation_container_code))
    WHERE separation_container_code IS NOT NULL;
