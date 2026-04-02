# mall-backend

Spring Boot starter project for an e-commerce mall system.

## Run

```bash
mvn spring-boot:run
```

## Auth

- Register endpoint: `POST /api/auth/register`
- Login endpoint: `POST /api/auth/login`
- Current user endpoint: `GET /api/auth/me`
- Seed admin account: `admin / admin123456`
- Protected endpoints require `Authorization: Bearer <token>`
- Admin-only endpoints: `GET /api/users`, `GET /api/roles`, `GET /api/permissions`

## Database

- PostgreSQL database: `mall_db`
- Default local user: `monscao`
- Health endpoint: `GET /api/system/health`
- Homepage aggregate endpoint: `GET /api/home`
- Catalog categories endpoint: `GET /api/catalog/categories`
- Catalog product list endpoint: `GET /api/catalog/products`
- Catalog product detail endpoint: `GET /api/catalog/products/{slug}`
- User list endpoint: `GET /api/users`
- Permission list endpoint: `GET /api/permissions`
- Role list endpoint: `GET /api/roles`

## Project structure

- `controller`: receives HTTP requests and returns responses
- `service`: contains business logic
- `repository`: reads and writes the database
- `model`: internal domain objects
- `dto`: API response objects

## Current mall foundation

- User, role, permission tables are initialized automatically
- Admin user is seeded on startup
- RBAC relation tables: `user_role`, `role_permission`, `user_permission`
- Catalog tables are initialized automatically with realistic mock storefront data
- Frontend-ready catalog entities: `category`, `product`, `product_sku`
- Apple Lite homepage contract is documented in `docs/apple-lite-front-end-plan.md`
