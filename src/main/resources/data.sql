insert into permission (code, name, description)
values
    ('USER:READ', 'View users', 'Allows viewing user information'),
    ('USER:WRITE', 'Manage users', 'Allows creating and updating users'),
    ('ROLE:READ', 'View roles', 'Allows viewing role information'),
    ('ROLE:WRITE', 'Manage roles', 'Allows creating and updating roles'),
    ('ROLE:ASSIGN', 'Assign permissions', 'Allows assigning permissions to users')
on conflict (code) do nothing;

insert into role (code, name, description)
values
    ('ADMIN', 'Administrator', 'Full management access for the mall system'),
    ('CUSTOMER', 'Customer', 'Default role for mall customers')
on conflict (code) do nothing;

insert into app_user (username, password_hash, nickname, email, phone)
values ('admin', '{noop}admin123456', 'System Admin', 'admin@shop.local', '13800000000')
on conflict (username) do update set
    password_hash = excluded.password_hash,
    nickname = excluded.nickname,
    email = excluded.email,
    phone = excluded.phone;

insert into role_permission (role_id, permission_id)
select r.id, p.id
from role r
join permission p on (
    (r.code = 'ADMIN' and p.code in ('USER:READ', 'USER:WRITE', 'ROLE:READ', 'ROLE:WRITE', 'ROLE:ASSIGN'))
    or (r.code = 'CUSTOMER' and p.code in ('USER:READ'))
)
on conflict (role_id, permission_id) do nothing;

insert into user_role (user_id, role_id)
select u.id, r.id
from app_user u
join role r on r.code = 'ADMIN'
where u.username = 'admin'
on conflict (user_id, role_id) do nothing;

insert into user_permission (user_id, permission_id)
select u.id, p.id
from app_user u
join permission p on p.code in ('USER:READ', 'USER:WRITE', 'ROLE:READ', 'ROLE:WRITE', 'ROLE:ASSIGN')
where u.username = 'admin'
on conflict (user_id, permission_id) do nothing;
