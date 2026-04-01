create table if not exists app_user (
    id bigserial primary key,
    username varchar(50) not null unique,
    password_hash varchar(255) not null,
    nickname varchar(100),
    email varchar(100) unique,
    phone varchar(30),
    enabled boolean not null default true,
    created_at timestamp not null default current_timestamp,
    updated_at timestamp not null default current_timestamp
);

create table if not exists permission (
    id bigserial primary key,
    code varchar(100) not null unique,
    name varchar(100) not null,
    description varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists role (
    id bigserial primary key,
    code varchar(100) not null unique,
    name varchar(100) not null,
    description varchar(255),
    created_at timestamp not null default current_timestamp
);

create table if not exists user_permission (
    user_id bigint not null,
    permission_id bigint not null,
    granted_at timestamp not null default current_timestamp,
    primary key (user_id, permission_id),
    constraint fk_user_permission_user
        foreign key (user_id) references app_user (id) on delete cascade,
    constraint fk_user_permission_permission
        foreign key (permission_id) references permission (id) on delete cascade
);

create table if not exists user_role (
    user_id bigint not null,
    role_id bigint not null,
    assigned_at timestamp not null default current_timestamp,
    primary key (user_id, role_id),
    constraint fk_user_role_user
        foreign key (user_id) references app_user (id) on delete cascade,
    constraint fk_user_role_role
        foreign key (role_id) references role (id) on delete cascade
);

create table if not exists role_permission (
    role_id bigint not null,
    permission_id bigint not null,
    granted_at timestamp not null default current_timestamp,
    primary key (role_id, permission_id),
    constraint fk_role_permission_role
        foreign key (role_id) references role (id) on delete cascade,
    constraint fk_role_permission_permission
        foreign key (permission_id) references permission (id) on delete cascade
);

create table if not exists category (
    id bigserial primary key,
    code varchar(50) not null unique,
    name varchar(100) not null,
    description varchar(255),
    icon varchar(255),
    banner_image varchar(255),
    sort_order integer not null default 0,
    featured boolean not null default false,
    created_at timestamp not null default current_timestamp
);

create table if not exists product (
    id bigserial primary key,
    category_id bigint not null,
    name varchar(150) not null,
    subtitle varchar(255),
    slug varchar(150) not null unique,
    brand varchar(100),
    cover_image varchar(255),
    price_from numeric(10, 2) not null,
    price_to numeric(10, 2) not null,
    market_price numeric(10, 2),
    rating numeric(3, 2) not null default 0,
    sales_count integer not null default 0,
    stock_status varchar(30) not null default 'IN_STOCK',
    tags varchar(255),
    description text,
    featured boolean not null default false,
    on_shelf boolean not null default true,
    created_at timestamp not null default current_timestamp,
    constraint fk_product_category
        foreign key (category_id) references category (id) on delete restrict
);

create table if not exists product_sku (
    id bigserial primary key,
    product_id bigint not null,
    sku_code varchar(80) not null unique,
    name varchar(150) not null,
    spec_summary varchar(255),
    sale_price numeric(10, 2) not null,
    market_price numeric(10, 2),
    stock integer not null default 0,
    cover_image varchar(255),
    is_default boolean not null default false,
    created_at timestamp not null default current_timestamp,
    constraint fk_product_sku_product
        foreign key (product_id) references product (id) on delete cascade
);
