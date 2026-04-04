package com.malllite.catalog.repository;

import com.malllite.catalog.model.Category;
import com.malllite.catalog.model.Product;
import com.malllite.catalog.model.ProductAsset;
import com.malllite.catalog.model.ProductSku;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.core.simple.JdbcClient.StatementSpec;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CatalogRepository {

    private static final RowMapper<Category> CATEGORY_ROW_MAPPER = (rs, rowNum) -> new Category(
            rs.getLong("id"),
            rs.getString("code"),
            rs.getString("name"),
            rs.getString("description"),
            rs.getString("icon"),
            rs.getString("banner_image"),
            rs.getInt("sort_order"),
            rs.getBoolean("featured")
    );

    private static final RowMapper<Product> PRODUCT_ROW_MAPPER = (rs, rowNum) -> new Product(
            rs.getLong("id"),
            rs.getLong("category_id"),
            rs.getString("category_code"),
            rs.getString("category_name"),
            rs.getString("name"),
            rs.getString("subtitle"),
            rs.getString("slug"),
            rs.getString("brand"),
            rs.getString("cover_image"),
            rs.getBigDecimal("price_from").toPlainString(),
            rs.getBigDecimal("price_to").toPlainString(),
            rs.getBigDecimal("market_price").toPlainString(),
            rs.getBigDecimal("rating").toPlainString(),
            rs.getInt("sales_count"),
            rs.getString("stock_status"),
            rs.getString("tags"),
            rs.getString("description"),
            rs.getBoolean("featured"),
            rs.getBoolean("on_shelf")
    );

    private static final RowMapper<ProductSku> PRODUCT_SKU_ROW_MAPPER = (rs, rowNum) -> new ProductSku(
            rs.getLong("id"),
            rs.getLong("product_id"),
            rs.getString("sku_code"),
            rs.getString("name"),
            rs.getString("spec_summary"),
            rs.getBigDecimal("sale_price").toPlainString(),
            rs.getBigDecimal("market_price").toPlainString(),
            rs.getInt("stock"),
            rs.getString("cover_image"),
            rs.getBoolean("is_default")
    );

    private static final RowMapper<ProductAsset> PRODUCT_ASSET_ROW_MAPPER = (rs, rowNum) -> new ProductAsset(
            rs.getLong("id"),
            rs.getLong("product_id"),
            rs.getString("image_url"),
            rs.getString("alt_text"),
            rs.getInt("sort_order")
    );

    private final JdbcClient jdbcClient;

    public CatalogRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<Category> findFeaturedCategories() {
        return jdbcClient.sql("""
                        select id, code, name, description, icon, banner_image, sort_order, featured
                        from category
                        where featured = true
                        order by sort_order, id
                        """)
                .query(CATEGORY_ROW_MAPPER)
                .list();
    }

    public List<Product> findProducts(
            String categoryCode,
            Boolean featuredOnly,
            String sort,
            String keyword,
            Integer limit,
            Integer offset
    ) {
        String sql = """
                select p.id, p.category_id, c.code as category_code, c.name as category_name,
                       p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                       p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                       p.description, p.featured, p.on_shelf
                from product p
                join category c on c.id = p.category_id
                where p.on_shelf = true
                """
                + buildProductFilterClause(categoryCode, featuredOnly, keyword)
                + " order by " + toOrderBy(sort)
                + " limit :limit offset :offset";

        return bindProductFilterParams(jdbcClient.sql(sql), categoryCode, featuredOnly, keyword)
                .param("limit", normalizeLimit(limit))
                .param("offset", Math.max(offset == null ? 0 : offset, 0))
                .query(PRODUCT_ROW_MAPPER)
                .list();
    }

    public long countProducts(String categoryCode, Boolean featuredOnly, String keyword) {
        String sql = """
                select count(*)
                from product p
                join category c on c.id = p.category_id
                where p.on_shelf = true
                """
                + buildProductFilterClause(categoryCode, featuredOnly, keyword);

        return bindProductFilterParams(jdbcClient.sql(sql), categoryCode, featuredOnly, keyword)
                .query(Long.class)
                .single();
    }

    public List<Product> findAdminProducts() {
        return jdbcClient.sql("""
                        select p.id, p.category_id, c.code as category_code, c.name as category_name,
                               p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                               p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                               p.description, p.featured, p.on_shelf
                        from product p
                        join category c on c.id = p.category_id
                        order by p.id desc
                        """)
                .query(PRODUCT_ROW_MAPPER)
                .list();
    }

    public Optional<Product> findProductBySlug(String slug) {
        return jdbcClient.sql("""
                        select p.id, p.category_id, c.code as category_code, c.name as category_name,
                               p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                               p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                               p.description, p.featured, p.on_shelf
                        from product p
                        join category c on c.id = p.category_id
                        where p.slug = :slug and p.on_shelf = true
                        """)
                .param("slug", slug)
                .query(PRODUCT_ROW_MAPPER)
                .optional();
    }

    public Optional<Product> findProductById(Long productId) {
        return jdbcClient.sql("""
                        select p.id, p.category_id, c.code as category_code, c.name as category_name,
                               p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                               p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                               p.description, p.featured, p.on_shelf
                        from product p
                        join category c on c.id = p.category_id
                        where p.id = :productId
                        """)
                .param("productId", productId)
                .query(PRODUCT_ROW_MAPPER)
                .optional();
    }

    public List<ProductSku> findSkusByProductId(Long productId) {
        return jdbcClient.sql("""
                        select id, product_id, sku_code, name, spec_summary, sale_price, market_price,
                               stock, cover_image, is_default
                        from product_sku
                        where product_id = :productId
                        order by is_default desc, sale_price asc, id
                        """)
                .param("productId", productId)
                .query(PRODUCT_SKU_ROW_MAPPER)
                .list();
    }

    public Optional<String> findDefaultSkuCodeByProductId(Long productId) {
        return jdbcClient.sql("""
                        select sku_code
                        from product_sku
                        where product_id = :productId
                        order by is_default desc, sale_price asc, id
                        limit 1
                        """)
                .param("productId", productId)
                .query(String.class)
                .optional();
    }

    public List<ProductAsset> findAssetsByProductId(Long productId) {
        return jdbcClient.sql("""
                        select id, product_id, image_url, alt_text, sort_order
                        from product_asset
                        where product_id = :productId
                        order by sort_order asc, id asc
                        """)
                .param("productId", productId)
                .query(PRODUCT_ASSET_ROW_MAPPER)
                .list();
    }

    public Optional<Long> findCategoryIdByCode(String categoryCode) {
        return jdbcClient.sql("""
                        select id
                        from category
                        where code = :categoryCode
                        """)
                .param("categoryCode", categoryCode)
                .query(Long.class)
                .optional();
    }

    public Long insertProduct(
            Long categoryId,
            String name,
            String subtitle,
            String slug,
            String brand,
            String coverImage,
            String coverImageKey,
            String coverImageName,
            String priceFrom,
            String priceTo,
            String marketPrice,
            String stockStatus,
            String tags,
            String description,
            Boolean featured,
            Boolean onShelf
    ) {
        return jdbcClient.sql("""
                        insert into product (
                            category_id, name, subtitle, slug, brand, cover_image, cover_image_key, cover_image_name, price_from, price_to, market_price,
                            rating, sales_count, stock_status, tags, description, featured, on_shelf
                        )
                        values (
                            :categoryId, :name, :subtitle, :slug, :brand, :coverImage, :coverImageKey, :coverImageName, cast(:priceFrom as numeric),
                            cast(:priceTo as numeric), cast(:marketPrice as numeric), cast(0 as numeric), 0, :stockStatus,
                            :tags, :description, :featured, :onShelf
                        )
                        returning id
                        """)
                .param("categoryId", categoryId)
                .param("name", name)
                .param("subtitle", subtitle)
                .param("slug", slug)
                .param("brand", brand)
                .param("coverImage", coverImage)
                .param("coverImageKey", coverImageKey)
                .param("coverImageName", coverImageName)
                .param("priceFrom", priceFrom)
                .param("priceTo", priceTo)
                .param("marketPrice", marketPrice)
                .param("stockStatus", stockStatus)
                .param("tags", tags)
                .param("description", description)
                .param("featured", featured)
                .param("onShelf", onShelf)
                .query(Long.class)
                .single();
    }

    public void insertSku(
            Long productId,
            String skuCode,
            String name,
            String specSummary,
            String salePrice,
            String marketPrice,
            Integer stock,
            String coverImage,
            Boolean isDefault
    ) {
        jdbcClient.sql("""
                        insert into product_sku (
                            product_id, sku_code, name, spec_summary, sale_price, market_price, stock, cover_image, is_default
                        )
                        values (
                            :productId, :skuCode, :name, :specSummary, cast(:salePrice as numeric),
                            cast(:marketPrice as numeric), :stock, :coverImage, :isDefault
                        )
                        """)
                .param("productId", productId)
                .param("skuCode", skuCode)
                .param("name", name)
                .param("specSummary", specSummary)
                .param("salePrice", salePrice)
                .param("marketPrice", marketPrice)
                .param("stock", stock)
                .param("coverImage", coverImage)
                .param("isDefault", isDefault)
                .update();
    }

    public void insertAsset(Long productId, String imageUrl, String imageKey, String originalName, String altText, Integer sortOrder) {
        jdbcClient.sql("""
                        insert into product_asset (product_id, image_url, image_key, original_name, alt_text, sort_order)
                        values (:productId, :imageUrl, :imageKey, :originalName, :altText, :sortOrder)
                        """)
                .param("productId", productId)
                .param("imageUrl", imageUrl)
                .param("imageKey", imageKey)
                .param("originalName", originalName)
                .param("altText", altText)
                .param("sortOrder", sortOrder)
                .update();
    }

    public void updateProduct(
            Long productId,
            Long categoryId,
            String name,
            String subtitle,
            String slug,
            String brand,
            String priceFrom,
            String priceTo,
            String marketPrice,
            String stockStatus,
            Boolean featured,
            Boolean onShelf
    ) {
        jdbcClient.sql("""
                        update product
                        set category_id = :categoryId,
                            name = :name,
                            subtitle = :subtitle,
                            slug = :slug,
                            brand = :brand,
                            price_from = cast(:priceFrom as numeric),
                            price_to = cast(:priceTo as numeric),
                            market_price = cast(:marketPrice as numeric),
                            stock_status = :stockStatus,
                            featured = :featured,
                            on_shelf = :onShelf
                        where id = :productId
                        """)
                .param("productId", productId)
                .param("categoryId", categoryId)
                .param("name", name)
                .param("subtitle", subtitle)
                .param("slug", slug)
                .param("brand", brand)
                .param("priceFrom", priceFrom)
                .param("priceTo", priceTo)
                .param("marketPrice", marketPrice)
                .param("stockStatus", stockStatus)
                .param("featured", featured)
                .param("onShelf", onShelf)
                .update();
    }

    public void deleteProduct(Long productId) {
        jdbcClient.sql("""
                        delete from product
                        where id = :productId
                        """)
                .param("productId", productId)
                .update();
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private Integer normalizeLimit(Integer limit) {
        if (limit == null || limit <= 0) {
            return 24;
        }
        return limit;
    }

    private String buildProductFilterClause(String categoryCode, Boolean featuredOnly, String keyword) {
        String normalizedCategoryCode = normalize(categoryCode);
        String normalizedKeyword = normalize(keyword);
        StringBuilder clause = new StringBuilder();

        if (normalizedCategoryCode != null) {
            clause.append(" and c.code = :categoryCode");
        }

        if (featuredOnly != null) {
            clause.append(" and p.featured = :featuredOnly");
        }

        if (normalizedKeyword != null) {
            clause.append("""
                     and (
                        lower(p.name) like lower(:keyword)
                        or lower(coalesce(p.subtitle, '')) like lower(:keyword)
                        or lower(coalesce(p.brand, '')) like lower(:keyword)
                        or lower(coalesce(p.tags, '')) like lower(:keyword)
                     )
                    """);
        }

        return clause.toString();
    }

    private StatementSpec bindProductFilterParams(
            StatementSpec statementSpec,
            String categoryCode,
            Boolean featuredOnly,
            String keyword
    ) {
        StatementSpec bound = statementSpec;
        String normalizedCategoryCode = normalize(categoryCode);
        String normalizedKeyword = normalize(keyword);

        if (normalizedCategoryCode != null) {
            bound = bound.param("categoryCode", normalizedCategoryCode);
        }

        if (featuredOnly != null) {
            bound = bound.param("featuredOnly", featuredOnly);
        }

        if (normalizedKeyword != null) {
            bound = bound.param("keyword", "%" + normalizedKeyword + "%");
        }

        return bound;
    }

    private String toOrderBy(String sort) {
        return switch (sort) {
            case "sales" -> "p.sales_count desc, p.rating desc, p.id";
            case "latest" -> "p.id desc";
            case "priceAsc" -> "p.price_from asc, p.id";
            case "priceDesc" -> "p.price_to desc, p.id";
            default -> "p.featured desc, p.sales_count desc, p.id";
        };
    }
}
