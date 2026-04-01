package com.shopback.catalog.repository;

import com.shopback.catalog.model.Category;
import com.shopback.catalog.model.Product;
import com.shopback.catalog.model.ProductSku;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
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

    public List<Product> findProducts(String categoryCode, Boolean featuredOnly, String sort, Integer limit) {
        String normalizedCategoryCode = normalize(categoryCode);
        String orderBy = toOrderBy(sort);
        Integer normalizedLimit = limit != null && limit > 0 ? limit : null;
        String limitClause = normalizedLimit == null ? "" : " limit " + normalizedLimit;

        if (normalizedCategoryCode != null && featuredOnly != null) {
            return jdbcClient.sql("""
                            select p.id, p.category_id, c.code as category_code, c.name as category_name,
                                   p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                                   p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                                   p.description, p.featured, p.on_shelf
                            from product p
                            join category c on c.id = p.category_id
                            where p.on_shelf = true
                              and c.code = :categoryCode
                              and p.featured = :featuredOnly
                            order by %s
                            %s
                            """.formatted(orderBy, limitClause))
                    .param("categoryCode", normalizedCategoryCode)
                    .param("featuredOnly", featuredOnly)
                    .query(PRODUCT_ROW_MAPPER)
                    .list();
        }

        if (normalizedCategoryCode != null) {
            return jdbcClient.sql("""
                            select p.id, p.category_id, c.code as category_code, c.name as category_name,
                                   p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                                   p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                                   p.description, p.featured, p.on_shelf
                            from product p
                            join category c on c.id = p.category_id
                            where p.on_shelf = true
                              and c.code = :categoryCode
                            order by %s
                            %s
                            """.formatted(orderBy, limitClause))
                    .param("categoryCode", normalizedCategoryCode)
                    .query(PRODUCT_ROW_MAPPER)
                    .list();
        }

        if (featuredOnly != null) {
            return jdbcClient.sql("""
                            select p.id, p.category_id, c.code as category_code, c.name as category_name,
                                   p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                                   p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                                   p.description, p.featured, p.on_shelf
                            from product p
                            join category c on c.id = p.category_id
                            where p.on_shelf = true
                              and p.featured = :featuredOnly
                            order by %s
                            %s
                            """.formatted(orderBy, limitClause))
                    .param("featuredOnly", featuredOnly)
                    .query(PRODUCT_ROW_MAPPER)
                    .list();
        }

        return jdbcClient.sql("""
                        select p.id, p.category_id, c.code as category_code, c.name as category_name,
                               p.name, p.subtitle, p.slug, p.brand, p.cover_image, p.price_from, p.price_to,
                               p.market_price, p.rating, p.sales_count, p.stock_status, p.tags,
                               p.description, p.featured, p.on_shelf
                        from product p
                        join category c on c.id = p.category_id
                        where p.on_shelf = true
                        order by %s
                        %s
                        """.formatted(orderBy, limitClause))
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

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value;
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
