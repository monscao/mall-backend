# Apple Lite Frontend Plan

## Direction

The storefront should feel premium and narrative-driven without sacrificing shopping efficiency.

- Homepage: cinematic, dark, scroll-led storytelling
- Catalog pages: clean and efficient
- Product detail pages: premium presentation with strong purchase clarity
- Cart and checkout: minimal and fast

## Page Structure

### Homepage

1. Hero scene
- Dark background
- Large white typography
- One flagship product as the visual anchor
- Slow, controlled motion and reveal

2. Story sections
- One section per product theme
- Split layout with oversized heading and focused product cards
- Recommended patterns: sticky text, parallax image, horizontal reveal

3. Featured categories
- Category entry cards with banner imagery
- More restrained than the hero

4. Hot and new products
- Lightweight product rows for fast browsing
- This is where shopping efficiency returns

### Catalog List Page

- Filter by category
- Sort by featured, sales, latest, priceAsc, priceDesc
- Product cards remain visually premium but readable

### Product Detail Page

- Large visual first screen
- Short marketing headline
- Immediate SKU selection and price clarity
- Description below the fold

## API Contract

### Homepage

`GET /api/home`

Returns:
- `theme`
- `hero`
- `featuredCategories`
- `sections`

### Catalog Categories

`GET /api/catalog/categories`

Returns:
- featured categories for navigation and homepage cards

### Catalog Products

`GET /api/catalog/products?categoryCode=phones&featured=true&sort=sales&limit=6`

Supported query params:
- `categoryCode`
- `featured`
- `sort`: `featured`, `sales`, `latest`, `priceAsc`, `priceDesc`
- `limit`

### Product Detail

`GET /api/catalog/products/{slug}`

Returns:
- product base info
- tags
- sku list

## Frontend Notes

- Do not use a classic banner carousel as the homepage shell
- Use one dominant hero instead of multiple competing panels
- Keep dark sections concentrated on homepage and special landing pages
- Keep list pages bright, readable, and quick to scan
