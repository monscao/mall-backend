# Mall Backend

Spring Boot backend for a full-stack mall practice project. It now includes authentication, finer-grained RBAC, persistent carts, order status flow, product publishing controls, and local image uploads.

## What This Project Covers

- JWT login and registration
- Role + permission based access control
- Catalog browsing and product detail APIs
- Admin product creation, editing, shelf toggling, and uploads
- Persistent per-user shopping carts
- Order creation, cancellation, and admin-side status progression
- PostgreSQL schema/data bootstrap on startup
- Integration tests with JaCoCo coverage checks

## Tech Stack

- Java 21 source level
- Spring Boot 3.3
- Spring MVC + JDBC
- PostgreSQL
- Maven
- JaCoCo for backend coverage checks

## Local Prerequisites

- Java 21+
- Maven 3.9+
- PostgreSQL 16+
- Database named `mall_db`

Default local configuration is in [`src/main/resources/application.yml`](/Users/monscao/Documents/mall-backend/src/main/resources/application.yml).

## Start The Backend

```bash
mvn spring-boot:run
```

Default backend URL: `http://localhost:8080`

Health check:

```bash
curl http://localhost:8080/api/system/health
```

## Database Notes

The app runs [`schema.sql`](/Users/monscao/Documents/mall-backend/src/main/resources/schema.sql) and [`data.sql`](/Users/monscao/Documents/mall-backend/src/main/resources/data.sql) on startup.

Seed data includes:

- admin user: `admin / admin123456`
- customer/admin roles
- permission codes for user, role, product, cart, and order modules
- demo categories, products, SKUs, and assets

Core tables:

- `app_user`
- `role`
- `permission`
- `user_role`
- `role_permission`
- `user_permission`
- `category`
- `product`
- `product_sku`
- `product_asset`
- `shopping_cart`
- `shopping_cart_item`
- `customer_order`
- `customer_order_item`

## Permission Model

Current permission codes:

- `USER:READ`
- `USER:WRITE`
- `ROLE:READ`
- `ROLE:WRITE`
- `ROLE:ASSIGN`
- `PRODUCT:READ`
- `PRODUCT:WRITE`
- `PRODUCT:PUBLISH`
- `PRODUCT:UPLOAD`
- `ORDER:READ`
- `ORDER:MANAGE`
- `CART:READ`
- `CART:WRITE`

Notes:

- `CUSTOMER` gets product browse, cart, and personal order permissions.
- `ADMIN` gets full management permissions.
- `/api/auth/me` returns both `roleCodes` and `permissionCodes`.

## Main API Areas

Auth:

- `POST /api/auth/register`
- `POST /api/auth/login`
- `GET /api/auth/me`

System:

- `GET /api/system/health`
- `GET /api/home`

Catalog:

- `GET /api/catalog/categories`
- `GET /api/catalog/products`
- `GET /api/catalog/products/{slug}`

Admin catalog:

- `GET /api/catalog/admin/products`
- `POST /api/catalog/admin/products`
- `PUT /api/catalog/admin/products/{productId}`
- `PUT /api/catalog/admin/products/{productId}/shelf`
- `DELETE /api/catalog/admin/products/{productId}`
- `POST /api/catalog/admin/uploads`

Cart:

- `GET /api/cart`
- `PUT /api/cart`
- `DELETE /api/cart`

Orders:

- `POST /api/orders`
- `GET /api/orders`
- `GET /api/orders/{orderId}`
- `DELETE /api/orders/{orderId}`
- `GET /api/orders/admin`
- `GET /api/orders/admin/{orderId}`
- `PUT /api/orders/admin/{orderId}/status`

Admin/user metadata:

- `GET /api/users`
- `GET /api/roles`
- `GET /api/permissions`

## Order Status Flow

Customer-facing flow:

- `PENDING_PAYMENT`
- `PAID`
- `PROCESSING`
- `SHIPPED`
- `COMPLETED`
- `CANCELLED`

Rules:

- card/wallet orders start at `PAID`
- cash-on-delivery orders start at `PENDING_PAYMENT`
- customers can cancel while the order is still actionable
- admins can progress the order through fulfillment states

## Project Structure

- `controller`: HTTP entrypoints
- `service`: business rules and orchestration
- `repository`: SQL access via `JdbcClient`
- `model`: internal domain snapshots
- `dto`: request/response contracts
- `common`: shared exception and storage helpers

## Testing

Run backend tests:

```bash
mvn test
```

This project now runs:

- integration tests for auth
- catalog browsing tests
- admin catalog/upload tests
- persistent cart tests
- order flow tests
- homepage tests
- JaCoCo coverage verification during `mvn test`

Latest local measured backend line coverage from `target/site/jacoco/jacoco.csv`: about `85.55%`.

## Frontend Pairing

The matching frontend lives at:

- `/Users/monscao/Documents/mall-frontend`

Frontend contract/design notes:

- [`docs/apple-lite-front-end-plan.md`](/Users/monscao/Documents/mall-backend/docs/apple-lite-front-end-plan.md)
