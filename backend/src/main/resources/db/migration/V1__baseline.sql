create sequence order_number_seq start with 1 increment by 1;

create table app_user (
    id uuid primary key,
    email varchar(180) not null,
    password_hash varchar(120) not null,
    display_name varchar(120) not null,
    role varchar(30) not null,
    enabled boolean not null default true,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_app_user_email unique (email),
    constraint ck_app_user_role check (role in ('ADMIN','OPERATOR','DRIVER','REPORT_VIEWER'))
);

create table refresh_token (
    id uuid primary key,
    user_id uuid not null references app_user(id),
    token_hash varchar(64) not null,
    expires_at timestamptz not null,
    revoked_at timestamptz,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_refresh_token_hash unique (token_hash)
);
create index ix_refresh_token_user on refresh_token(user_id);
create index ix_refresh_token_expiry on refresh_token(expires_at) where revoked_at is null and deleted_at is null;

create table zone (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(120) not null,
    locality varchar(120) not null,
    active boolean not null default true,
    valid_from date not null,
    valid_to date,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_zone_code unique (code),
    constraint ck_zone_validity check (valid_to is null or valid_to >= valid_from)
);

create table customer (
    id uuid primary key,
    first_name varchar(100) not null,
    last_name varchar(100) not null,
    phone varchar(40) not null,
    whatsapp varchar(40),
    email varchar(180),
    document varchar(30),
    status varchar(20) not null,
    acquisition_source varchar(100),
    referred_by_customer_id uuid references customer(id),
    notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint ck_customer_status check (status in ('ACTIVE','INACTIVE','BLOCKED'))
);
create index ix_customer_name on customer(last_name, first_name) where deleted_at is null;
create index ix_customer_phone on customer(phone) where deleted_at is null;

create table customer_address (
    id uuid primary key,
    customer_id uuid not null references customer(id),
    zone_id uuid not null references zone(id),
    street varchar(160) not null,
    street_number varchar(30) not null,
    neighborhood varchar(100),
    locality varchar(120) not null,
    address_references text,
    latitude numeric(10,7),
    longitude numeric(10,7),
    is_primary boolean not null default false,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz
);
create index ix_address_customer on customer_address(customer_id) where deleted_at is null;
create unique index uk_customer_primary_address on customer_address(customer_id)
    where is_primary = true and deleted_at is null;

create table customer_preference (
    id uuid primary key,
    customer_id uuid not null references customer(id),
    fragrance varchar(80),
    fragrance_intensity varchar(30),
    soap_type varchar(80),
    softener_allowed boolean not null default true,
    allergy_notes text,
    baby_clothes boolean not null default false,
    dryer_allowed boolean not null default true,
    max_temperature_celsius integer,
    color_mix_allowed boolean not null default false,
    exclusive_cycle boolean not null default false,
    stain_treatment boolean not null default false,
    preferred_payment_method varchar(40),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_customer_preference unique (customer_id),
    constraint ck_customer_max_temperature check (
        max_temperature_celsius is null or max_temperature_celsius between 0 and 95
    )
);

create table garment_equivalence (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(120) not null,
    category varchar(80) not null,
    physical_units_per_group integer not null,
    equivalent_units numeric(10,2) not null,
    estimated_weight_grams integer,
    estimated_volume numeric(10,2),
    common_wash_allowed boolean not null,
    dryer_allowed boolean not null,
    exclusive_cycle_required boolean not null,
    quote_required boolean not null,
    active boolean not null,
    valid_from date not null,
    valid_to date,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint ck_garment_group_size check (physical_units_per_group > 0),
    constraint ck_garment_equivalent_units check (equivalent_units > 0),
    constraint ck_garment_estimated_weight check (estimated_weight_grams is null or estimated_weight_grams > 0),
    constraint ck_garment_validity check (valid_to is null or valid_to >= valid_from)
);
create unique index uk_garment_code_valid_from on garment_equivalence(code, valid_from);
create index ix_garment_active on garment_equivalence(category, name) where active = true and deleted_at is null;

create table service_plan (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(120) not null,
    description text,
    max_equivalent_units numeric(10,2),
    max_weight_grams integer,
    active boolean not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_service_plan_code unique (code),
    constraint ck_plan_equivalent_limit check (max_equivalent_units is null or max_equivalent_units > 0),
    constraint ck_plan_weight_limit check (max_weight_grams is null or max_weight_grams > 0)
);

create table price_version (
    id uuid primary key,
    service_plan_id uuid not null references service_plan(id),
    zone_id uuid references zone(id),
    amount numeric(19,2) not null,
    currency varchar(3) not null,
    customer_type varchar(30),
    channel varchar(30),
    modality varchar(30),
    valid_from date not null,
    valid_to date,
    reason varchar(300) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint ck_price_amount check (amount >= 0),
    constraint ck_price_validity check (valid_to is null or valid_to >= valid_from)
);
create index ix_price_lookup on price_version(service_plan_id, zone_id, valid_from, valid_to)
    where deleted_at is null;

create table promotion (
    id uuid primary key,
    code varchar(50) not null,
    name varchar(140) not null,
    description text,
    starts_on date not null,
    ends_on date,
    total_quota integer,
    daily_quota integer,
    monthly_quota integer,
    new_customers_only boolean not null default false,
    one_per_address boolean not null default false,
    stackable boolean not null default false,
    fixed_price numeric(19,2),
    percentage_discount numeric(7,4),
    credit_amount numeric(19,2),
    active boolean not null,
    cancellation_reason varchar(300),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_promotion_code unique (code),
    constraint ck_promotion_validity check (ends_on is null or ends_on >= starts_on),
    constraint ck_promotion_quota check (
        (total_quota is null or total_quota >= 0)
        and (daily_quota is null or daily_quota >= 0)
        and (monthly_quota is null or monthly_quota >= 0)
    ),
    constraint ck_promotion_percentage check (
        percentage_discount is null or (percentage_discount >= 0 and percentage_discount <= 1)
    ),
    constraint ck_promotion_value check (
        fixed_price is not null or percentage_discount is not null or credit_amount is not null
    )
);

create table laundry_order (
    id uuid primary key,
    order_number varchar(20) not null,
    customer_id uuid not null references customer(id),
    address_id uuid not null references customer_address(id),
    service_plan_id uuid not null references service_plan(id),
    price_version_id uuid references price_version(id),
    promotion_id uuid references promotion(id),
    status varchar(40) not null,
    modality varchar(30) not null,
    exclusive_cycle boolean not null default false,
    physical_piece_count integer not null default 0,
    equivalent_units numeric(10,2) not null default 0,
    declared_weight_grams integer,
    actual_weight_grams integer,
    quoted_base_amount numeric(19,2),
    discount_amount numeric(19,2),
    credit_amount numeric(19,2),
    quoted_amount numeric(19,2),
    confirmed_amount numeric(19,2),
    currency varchar(3) not null default 'ARS',
    pricing_explanation text,
    pickup_scheduled_at timestamptz,
    promised_at timestamptz,
    delivered_at timestamptz,
    notes text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint uk_laundry_order_number unique (order_number),
    constraint ck_order_weight check (
        (declared_weight_grams is null or declared_weight_grams >= 0)
        and (actual_weight_grams is null or actual_weight_grams >= 0)
    ),
    constraint ck_order_counts check (physical_piece_count >= 0 and equivalent_units >= 0),
    constraint ck_order_money check (
        (quoted_base_amount is null or quoted_base_amount >= 0)
        and (discount_amount is null or discount_amount >= 0)
        and (credit_amount is null or credit_amount >= 0)
        and (quoted_amount is null or quoted_amount >= 0)
        and (confirmed_amount is null or confirmed_amount >= 0)
    )
);
create index ix_order_customer on laundry_order(customer_id, created_at desc) where deleted_at is null;
create index ix_order_status on laundry_order(status, promised_at) where deleted_at is null;
create index ix_order_pickup on laundry_order(pickup_scheduled_at) where deleted_at is null;

create table order_item (
    id uuid primary key,
    order_id uuid not null references laundry_order(id),
    garment_equivalence_id uuid not null references garment_equivalence(id),
    rule_name_snapshot varchar(120) not null,
    physical_units_per_group_snapshot integer not null,
    equivalent_units_per_group_snapshot numeric(10,2) not null,
    physical_piece_count integer not null,
    group_count integer not null,
    equivalent_units numeric(10,2) not null,
    observations text,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint ck_order_item_counts check (
        physical_units_per_group_snapshot > 0
        and equivalent_units_per_group_snapshot > 0
        and physical_piece_count > 0
        and group_count > 0
        and equivalent_units > 0
    )
);
create index ix_order_item_order on order_item(order_id) where deleted_at is null;

create table order_status_history (
    id uuid primary key,
    order_id uuid not null references laundry_order(id),
    previous_status varchar(40),
    new_status varchar(40) not null,
    observation text,
    location varchar(200),
    notification_reference varchar(200),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz
);
create index ix_order_status_history on order_status_history(order_id, created_at);

create table promotion_usage (
    id uuid primary key,
    promotion_id uuid not null references promotion(id),
    customer_id uuid not null references customer(id),
    address_id uuid not null references customer_address(id),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz
);
create unique index uk_promotion_address_usage on promotion_usage(promotion_id, address_id)
    where deleted_at is null;
create index ix_promotion_usage_period on promotion_usage(promotion_id, created_at)
    where deleted_at is null;

create table payment (
    id uuid primary key,
    order_id uuid not null references laundry_order(id),
    customer_id uuid not null references customer(id),
    amount numeric(19,2) not null,
    currency varchar(3) not null,
    paid_at timestamptz not null,
    method_code varchar(40) not null,
    reference varchar(200),
    status varchar(30) not null,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz,
    constraint ck_payment_amount check (amount > 0),
    constraint ck_payment_status check (status in ('PENDING','PARTIAL','PAID','OVERDUE','REFUNDED','CANCELLED'))
);
create index ix_payment_order on payment(order_id, paid_at) where deleted_at is null;

create table audit_log (
    id uuid primary key,
    entity_type varchar(100) not null,
    entity_id uuid not null,
    action varchar(80) not null,
    old_value text,
    new_value text,
    reason varchar(500),
    created_at timestamptz not null,
    updated_at timestamptz not null,
    created_by uuid,
    updated_by uuid,
    version bigint not null default 0,
    deleted_at timestamptz
);
create index ix_audit_entity on audit_log(entity_type, entity_id, created_at desc);

-- Catálogos sembrados ahora para evitar hardcodes; sus módulos operativos se implementan por fases.
create table payment_method (
    code varchar(40) primary key,
    name varchar(100) not null,
    active boolean not null default true
);

create table equipment (
    id uuid primary key,
    equipment_type varchar(40) not null,
    brand varchar(80) not null,
    model varchar(100) not null,
    capacity_grams integer,
    status varchar(30) not null,
    purchase_date date,
    purchase_price numeric(19,2),
    currency varchar(3) not null default 'ARS',
    created_at timestamptz not null,
    updated_at timestamptz not null,
    constraint ck_equipment_capacity check (capacity_grams is null or capacity_grams > 0)
);

create table claim_type (
    code varchar(40) primary key,
    name varchar(100) not null,
    active boolean not null default true
);

create table whatsapp_template (
    code varchar(60) primary key,
    name varchar(120) not null,
    body text not null,
    active boolean not null default true,
    version integer not null default 1
);

create table service_policy (
    id uuid primary key,
    code varchar(60) not null,
    version integer not null,
    title varchar(160) not null,
    content text not null,
    valid_from date not null,
    valid_to date,
    active boolean not null,
    constraint uk_service_policy_version unique (code, version),
    constraint ck_service_policy_validity check (valid_to is null or valid_to >= valid_from)
);
