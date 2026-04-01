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
