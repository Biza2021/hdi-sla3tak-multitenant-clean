create table shops (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    business_name varchar(150) not null,
    slug varchar(80) not null unique
);

create table shop_users (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    shop_id bigint not null references shops(id) on delete cascade,
    full_name varchar(120) not null,
    username varchar(60) not null,
    password_hash varchar(255) not null,
    role varchar(20) not null,
    enabled boolean not null default true,
    constraint uk_shop_users_shop_username unique (shop_id, username)
);

create index idx_shop_users_shop_id on shop_users (shop_id);

create table customers (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    shop_id bigint not null references shops(id) on delete cascade,
    full_name varchar(120) not null,
    primary_phone varchar(40),
    secondary_phone varchar(40),
    notes text,
    constraint uk_customers_shop_primary_phone unique (shop_id, primary_phone)
);

create index idx_customers_shop_id on customers (shop_id);

create table repair_items (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    shop_id bigint not null references shops(id) on delete cascade,
    customer_id bigint not null references customers(id) on delete restrict,
    title varchar(160) not null,
    description text,
    status varchar(40) not null,
    repair_notes text,
    expected_delivery_date date,
    estimated_price numeric(12, 2) not null default 0,
    deposit_paid numeric(12, 2) not null default 0,
    remaining_balance numeric(12, 2) not null default 0,
    public_tracking_token varchar(80) not null unique,
    pickup_code varchar(40) not null,
    constraint uk_repair_items_shop_pickup_code unique (shop_id, pickup_code)
);

create index idx_repair_items_shop_id on repair_items (shop_id);
create index idx_repair_items_customer_id on repair_items (customer_id);

create table repair_status_history (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    shop_id bigint not null references shops(id) on delete cascade,
    repair_item_id bigint not null references repair_items(id) on delete cascade,
    changed_by_user_id bigint references shop_users(id) on delete set null,
    status varchar(40) not null,
    notes text
);

create index idx_repair_status_history_shop_id on repair_status_history (shop_id);
create index idx_repair_status_history_item_id on repair_status_history (repair_item_id);

create table repair_images (
    id bigserial primary key,
    created_at timestamptz not null,
    updated_at timestamptz not null,
    shop_id bigint not null references shops(id) on delete cascade,
    repair_item_id bigint not null references repair_items(id) on delete cascade,
    storage_key varchar(255) not null,
    original_filename varchar(255) not null,
    content_type varchar(120) not null,
    file_size bigint not null,
    visible_on_public_tracking boolean not null default false
);

create index idx_repair_images_shop_id on repair_images (shop_id);
create index idx_repair_images_item_id on repair_images (repair_item_id);

