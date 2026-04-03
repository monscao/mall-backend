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

## Docker

Build a backend image locally:

```bash
docker build -t mall-backend:local .
```

Useful runtime environment variables:

- `SPRING_DATASOURCE_URL`
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`
- `AUTH_JWT_SECRET`
- `APP_UPLOAD_DIR`
- `SERVER_PORT`

The provided [`Dockerfile`](/Users/monscao/Documents/mall-backend/Dockerfile) stores uploads under `/app/uploads` inside the container.

## Deployment

This repo now includes a minimal production deployment skeleton in [`deploy/docker-compose.prod.yml`](/Users/monscao/Documents/mall-backend/deploy/docker-compose.prod.yml).

Recommended first deployment path:

1. Buy one Linux server and install Docker Engine + Docker Compose plugin.
2. Clone this repo on the server.
3. Copy [`deploy/.env.example`](/Users/monscao/Documents/mall-backend/deploy/.env.example) to `.env.prod` and replace the placeholder secrets.
4. Set `BACKEND_IMAGE` and `FRONTEND_IMAGE` to the GitHub Container Registry image names you publish from the two repos.
5. Run:

```bash
cd deploy
docker compose --env-file .env.prod -f docker-compose.prod.yml pull
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d
```

The compose file starts:

- `postgres`
- `backend`
- `frontend`

The frontend container serves the React build with Nginx and proxies `/api/*` and `/uploads/*` to the backend container.

## CI/CD

Included GitHub Actions:

- [`Backend CI`](/Users/monscao/Documents/mall-backend/.github/workflows/ci.yml): runs `mvn test` and validates the Docker build
- [`Backend Publish`](/Users/monscao/Documents/mall-backend/.github/workflows/publish.yml): builds and pushes `ghcr.io/<owner>/mall-backend`
- [`Deploy To Server`](/Users/monscao/Documents/mall-backend/.github/workflows/deploy.yml): manually deploys the published frontend/backend images to your server

Secrets needed for the deploy workflow:

- `DEPLOY_HOST`
- `DEPLOY_USER`
- `DEPLOY_SSH_KEY`
- `DEPLOY_PATH`
- `GHCR_USERNAME`
- `GHCR_TOKEN`
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `AUTH_JWT_SECRET`
- `FRONTEND_PORT`

## Frontend Pairing

The matching frontend lives at:

- `/Users/monscao/Documents/mall-frontend`

Frontend contract/design notes:

- [`docs/apple-lite-front-end-plan.md`](/Users/monscao/Documents/mall-backend/docs/apple-lite-front-end-plan.md)
