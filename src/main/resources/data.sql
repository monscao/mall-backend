insert into permission (code, name, description)
values
    ('USER:READ', 'View users', 'Allows viewing user information'),
    ('USER:WRITE', 'Manage users', 'Allows creating and updating users'),
    ('ROLE:READ', 'View roles', 'Allows viewing role information'),
    ('ROLE:WRITE', 'Manage roles', 'Allows creating and updating roles'),
    ('ROLE:ASSIGN', 'Assign permissions', 'Allows assigning permissions to users'),
    ('PRODUCT:READ', 'View products', 'Allows viewing product information'),
    ('PRODUCT:WRITE', 'Manage products', 'Allows creating and updating products'),
    ('PRODUCT:PUBLISH', 'Publish products', 'Allows changing product shelf status'),
    ('PRODUCT:UPLOAD', 'Upload product assets', 'Allows uploading product assets'),
    ('ORDER:READ', 'View orders', 'Allows viewing order information'),
    ('ORDER:MANAGE', 'Manage orders', 'Allows progressing or cancelling any order'),
    ('CART:READ', 'View carts', 'Allows viewing cart information'),
    ('CART:WRITE', 'Manage carts', 'Allows updating cart information')
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
    (r.code = 'ADMIN' and p.code in (
        'USER:READ', 'USER:WRITE', 'ROLE:READ', 'ROLE:WRITE', 'ROLE:ASSIGN',
        'PRODUCT:READ', 'PRODUCT:WRITE', 'PRODUCT:PUBLISH', 'PRODUCT:UPLOAD',
        'ORDER:READ', 'ORDER:MANAGE', 'CART:READ', 'CART:WRITE'
    ))
    or (r.code = 'CUSTOMER' and p.code in ('PRODUCT:READ', 'ORDER:READ', 'CART:READ', 'CART:WRITE'))
)
on conflict (role_id, permission_id) do nothing;

delete from role_permission
where role_id in (select id from role where code = 'CUSTOMER')
  and permission_id in (select id from permission where code = 'USER:READ');

insert into user_role (user_id, role_id)
select u.id, r.id
from app_user u
join role r on r.code = 'ADMIN'
where u.username = 'admin'
on conflict (user_id, role_id) do nothing;

insert into user_permission (user_id, permission_id)
select u.id, p.id
from app_user u
join permission p on p.code in (
    'USER:READ', 'USER:WRITE', 'ROLE:READ', 'ROLE:WRITE', 'ROLE:ASSIGN',
    'PRODUCT:READ', 'PRODUCT:WRITE', 'PRODUCT:PUBLISH', 'PRODUCT:UPLOAD',
    'ORDER:READ', 'ORDER:MANAGE', 'CART:READ', 'CART:WRITE'
)
where u.username = 'admin'
on conflict (user_id, permission_id) do nothing;

insert into category (code, name, description, icon, banner_image, sort_order, featured)
values
    ('phones', 'Phones', 'Popular flagship and mid-range smartphones', 'smartphone', 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=1200&q=80', 1, true),
    ('laptops', 'Laptops', 'Thin-and-light laptops and creator notebooks', 'laptop', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 2, true),
    ('audio', 'Audio', 'Headphones, speakers and listening accessories', 'headphones', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=80', 3, true),
    ('wearables', 'Wearables', 'Smart watches and fitness gear', 'watch', 'https://images.unsplash.com/photo-1546868871-7041f2a55e12?auto=format&fit=crop&w=1200&q=80', 4, false)
on conflict (code) do update set
    name = excluded.name,
    description = excluded.description,
    icon = excluded.icon,
    banner_image = excluded.banner_image,
    sort_order = excluded.sort_order,
    featured = excluded.featured;

insert into product (
    category_id, name, subtitle, slug, brand, cover_image, price_from, price_to, market_price,
    rating, sales_count, stock_status, tags, description, featured, on_shelf
)
select c.id, p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to, p.market_price,
       p.rating, p.sales_count, p.stock_status, p.tags, p.description, p.featured, p.on_shelf
from (
    values
        ('phones', 'Nova X Pro', '120Hz OLED flagship with all-day battery', 'nova-x-pro', 'NovaTech', 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80', 4999.00, 5499.00, 5999.00, 4.80, 12864, 'IN_STOCK', '旗舰,热销,5G,拍照', 'A flagship phone built for smooth scrolling, low-light photos and daily reliability. Great as the homepage hero product for a premium electronics storefront.', true, true),
        ('phones', 'Pixel Wave Lite', 'Balanced mid-range phone for everyday use', 'pixel-wave-lite', 'Wave', 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=900&q=80', 2299.00, 2599.00, 2899.00, 4.60, 9321, 'IN_STOCK', '高性价比,学生党,轻薄', 'A practical phone with stable performance, light body and a friendly price point for activity pages and product cards.', false, true),
        ('laptops', 'AeroBook 14', 'Portable laptop for office and study', 'aerobook-14', 'Aero', 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80', 5699.00, 6699.00, 7299.00, 4.70, 4210, 'IN_STOCK', '办公本,轻薄,长续航', 'A 14-inch laptop focused on portability, long battery life and low-noise performance. Useful for building list pages that need office-oriented products.', true, true),
        ('laptops', 'Forge Studio 16', 'Performance notebook for design and rendering', 'forge-studio-16', 'Forge', 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?auto=format&fit=crop&w=900&q=80', 8999.00, 10999.00, 11999.00, 4.90, 1938, 'LOW_STOCK', '设计师,高性能,独显', 'A creator laptop with high refresh screen and discrete graphics. Works well for premium recommendation sections and detail pages.', true, true),
        ('audio', 'EchoBeat Pro', 'Active noise cancelling over-ear headphones', 'echobeat-pro', 'EchoBeat', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80', 899.00, 1099.00, 1299.00, 4.75, 26541, 'IN_STOCK', '降噪,通勤,蓝牙', 'A popular headphone option for new-user recommendation modules and seasonal promotions.', true, true),
        ('wearables', 'Pulse Fit S', 'Smart watch for fitness and daily reminders', 'pulse-fit-s', 'Pulse', 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80', 799.00, 999.00, 1199.00, 4.55, 6820, 'IN_STOCK', '手表,运动,健康', 'An entry-friendly smartwatch that adds life to category pages and personalized recommendation zones.', false, true)
) as p(category_code, name, subtitle, slug, brand, cover_image, price_from, price_to, market_price, rating, sales_count, stock_status, tags, description, featured, on_shelf)
join category c on c.code = p.category_code
on conflict (slug) do update set
    category_id = excluded.category_id,
    name = excluded.name,
    subtitle = excluded.subtitle,
    brand = excluded.brand,
    cover_image = excluded.cover_image,
    price_from = excluded.price_from,
    price_to = excluded.price_to,
    market_price = excluded.market_price,
    rating = excluded.rating,
    sales_count = excluded.sales_count,
    stock_status = excluded.stock_status,
    tags = excluded.tags,
    description = excluded.description,
    featured = excluded.featured,
    on_shelf = excluded.on_shelf;

insert into product_sku (product_id, sku_code, name, spec_summary, sale_price, market_price, stock, cover_image, is_default)
select p.id, s.sku_code, s.name, s.spec_summary, s.sale_price, s.market_price, s.stock, s.cover_image, s.is_default
from (
    values
        ('nova-x-pro', 'NXP-12-256-BLK', 'Nova X Pro 12GB+256GB', 'Midnight Black / 12GB / 256GB', 4999.00, 5999.00, 82, 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80', true),
        ('nova-x-pro', 'NXP-16-512-SLV', 'Nova X Pro 16GB+512GB', 'Moon Silver / 16GB / 512GB', 5499.00, 5999.00, 35, 'https://images.unsplash.com/photo-1598327105666-5b89351aff97?auto=format&fit=crop&w=900&q=80', false),
        ('pixel-wave-lite', 'PWL-8-128-BLU', 'Pixel Wave Lite 8GB+128GB', 'Sky Blue / 8GB / 128GB', 2299.00, 2899.00, 126, 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=900&q=80', true),
        ('pixel-wave-lite', 'PWL-12-256-WHT', 'Pixel Wave Lite 12GB+256GB', 'Snow White / 12GB / 256GB', 2599.00, 2899.00, 64, 'https://images.unsplash.com/photo-1510557880182-3d4d3cba35a5?auto=format&fit=crop&w=900&q=80', false),
        ('aerobook-14', 'AB14-I5-16-512', 'AeroBook 14 i5 16GB 512GB', 'Silver / i5 / 16GB / 512GB', 5699.00, 7299.00, 49, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80', true),
        ('aerobook-14', 'AB14-I7-32-1T', 'AeroBook 14 i7 32GB 1TB', 'Gray / i7 / 32GB / 1TB', 6699.00, 7299.00, 19, 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=80', false),
        ('forge-studio-16', 'FS16-R7-32-1T', 'Forge Studio 16 R7 32GB 1TB', 'Graphite / R7 / 32GB / 1TB', 8999.00, 11999.00, 12, 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?auto=format&fit=crop&w=900&q=80', true),
        ('forge-studio-16', 'FS16-R9-64-2T', 'Forge Studio 16 R9 64GB 2TB', 'Graphite / R9 / 64GB / 2TB', 10999.00, 11999.00, 5, 'https://images.unsplash.com/photo-1525547719571-a2d4ac8945e2?auto=format&fit=crop&w=900&q=80', false),
        ('echobeat-pro', 'EBP-STD-BLK', 'EchoBeat Pro Standard', 'Black / ANC / 40h battery', 899.00, 1299.00, 155, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80', true),
        ('echobeat-pro', 'EBP-PLUS-CRM', 'EchoBeat Pro Plus', 'Cream / ANC / wireless charging case', 1099.00, 1299.00, 73, 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=900&q=80', false),
        ('pulse-fit-s', 'PFS-SIL-STD', 'Pulse Fit S Standard', 'Silver / Silicone strap', 799.00, 1199.00, 91, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80', true),
        ('pulse-fit-s', 'PFS-BLK-SPR', 'Pulse Fit S Sport', 'Black / Sport strap', 999.00, 1199.00, 48, 'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=900&q=80', false)
) as s(product_slug, sku_code, name, spec_summary, sale_price, market_price, stock, cover_image, is_default)
join product p on p.slug = s.product_slug
on conflict (sku_code) do update set
    product_id = excluded.product_id,
    name = excluded.name,
    spec_summary = excluded.spec_summary,
    sale_price = excluded.sale_price,
    market_price = excluded.market_price,
    stock = excluded.stock,
    cover_image = excluded.cover_image,
    is_default = excluded.is_default;

insert into product_asset (product_id, image_url, alt_text, sort_order)
select p.id, a.image_url, a.alt_text, a.sort_order
from (
    values
        ('nova-x-pro', 'https://images.unsplash.com/photo-1512499617640-c74ae3a79d37?auto=format&fit=crop&w=1200&q=80', 'Nova X Pro lifestyle', 1),
        ('nova-x-pro', 'https://images.unsplash.com/photo-1580910051074-3eb694886505?auto=format&fit=crop&w=1200&q=80', 'Nova X Pro in blue light', 2),
        ('nova-x-pro', 'https://images.unsplash.com/photo-1567581935884-3349723552ca?auto=format&fit=crop&w=1200&q=80', 'Nova X Pro close-up', 3),
        ('aerobook-14', 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1200&q=80', 'AeroBook workspace', 1),
        ('aerobook-14', 'https://images.unsplash.com/photo-1498050108023-c5249f4df085?auto=format&fit=crop&w=1200&q=80', 'AeroBook desktop setup', 2),
        ('aerobook-14', 'https://images.unsplash.com/photo-1515879218367-8466d910aaa4?auto=format&fit=crop&w=1200&q=80', 'AeroBook keyboard detail', 3),
        ('echobeat-pro', 'https://images.unsplash.com/photo-1484704849700-f032a568e944?auto=format&fit=crop&w=1200&q=80', 'EchoBeat listening setup', 1),
        ('echobeat-pro', 'https://images.unsplash.com/photo-1546435770-a3e426bf472b?auto=format&fit=crop&w=1200&q=80', 'EchoBeat premium finish', 2),
        ('echobeat-pro', 'https://images.unsplash.com/photo-1507878866276-a947ef722fee?auto=format&fit=crop&w=1200&q=80', 'EchoBeat travel case', 3)
) as a(product_slug, image_url, alt_text, sort_order)
join product p on p.slug = a.product_slug
where not exists (
    select 1
    from product_asset existing
    where existing.product_id = p.id
      and existing.image_url = a.image_url
);
