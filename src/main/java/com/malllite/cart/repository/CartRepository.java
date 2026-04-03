package com.malllite.cart.repository;

import com.malllite.cart.model.CartItemSnapshot;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class CartRepository {

    private static final RowMapper<CartItemSnapshot> CART_ITEM_ROW_MAPPER = (rs, rowNum) -> new CartItemSnapshot(
            rs.getLong("id"),
            rs.getLong("cart_id"),
            rs.getLong("product_id"),
            rs.getLong("sku_id"),
            rs.getString("product_slug"),
            rs.getString("product_name"),
            rs.getString("sku_code"),
            rs.getString("sku_name"),
            rs.getString("spec_summary"),
            rs.getString("cover_image"),
            rs.getBigDecimal("sale_price").toPlainString(),
            rs.getBigDecimal("market_price") == null ? null : rs.getBigDecimal("market_price").toPlainString(),
            rs.getInt("stock"),
            rs.getInt("quantity")
    );

    private final JdbcClient jdbcClient;

    public CartRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long findOrCreateCartId(Long userId) {
        Optional<Long> existing = jdbcClient.sql("""
                        select id
                        from shopping_cart
                        where user_id = :userId
                        """)
                .param("userId", userId)
                .query(Long.class)
                .optional();

        if (existing.isPresent()) {
            return existing.get();
        }

        return jdbcClient.sql("""
                        insert into shopping_cart (user_id)
                        values (:userId)
                        returning id
                        """)
                .param("userId", userId)
                .query(Long.class)
                .single();
    }

    public List<CartItemSnapshot> findItemsByCartId(Long cartId) {
        return jdbcClient.sql("""
                        select sci.id, sci.cart_id, sci.product_id, sci.sku_id,
                               p.slug as product_slug, p.name as product_name,
                               sku.sku_code, sku.name as sku_name, sku.spec_summary,
                               coalesce(sku.cover_image, p.cover_image) as cover_image,
                               sku.sale_price, sku.market_price, sku.stock, sci.quantity
                        from shopping_cart_item sci
                        join product p on p.id = sci.product_id
                        join product_sku sku on sku.id = sci.sku_id
                        where sci.cart_id = :cartId
                          and p.on_shelf = true
                        order by sci.id asc
                        """)
                .param("cartId", cartId)
                .query(CART_ITEM_ROW_MAPPER)
                .list();
    }

    public Optional<CartItemSnapshot> findSkuSnapshotBySkuCode(String skuCode) {
        return jdbcClient.sql("""
                        select cast(0 as bigint) as id, cast(0 as bigint) as cart_id, p.id as product_id, sku.id as sku_id,
                               p.slug as product_slug, p.name as product_name,
                               sku.sku_code, sku.name as sku_name, sku.spec_summary,
                               coalesce(sku.cover_image, p.cover_image) as cover_image,
                               sku.sale_price, sku.market_price, sku.stock, cast(0 as integer) as quantity
                        from product_sku sku
                        join product p on p.id = sku.product_id
                        where sku.sku_code = :skuCode
                          and p.on_shelf = true
                        """)
                .param("skuCode", skuCode)
                .query(CART_ITEM_ROW_MAPPER)
                .optional();
    }

    public void replaceItems(Long cartId, List<CartItemSnapshot> items) {
        clearCart(cartId);
        for (CartItemSnapshot item : items) {
            upsertCartItem(cartId, item.productId(), item.skuId(), item.quantity());
        }
        touchCart(cartId);
    }

    public void upsertCartItem(Long cartId, Long productId, Long skuId, Integer quantity) {
        jdbcClient.sql("""
                        insert into shopping_cart_item (cart_id, product_id, sku_id, quantity)
                        values (:cartId, :productId, :skuId, :quantity)
                        on conflict (cart_id, sku_id)
                        do update set quantity = excluded.quantity,
                                      updated_at = current_timestamp
                        """)
                .param("cartId", cartId)
                .param("productId", productId)
                .param("skuId", skuId)
                .param("quantity", quantity)
                .update();
        touchCart(cartId);
    }

    public void removeItemBySkuCode(Long cartId, String skuCode) {
        jdbcClient.sql("""
                        delete from shopping_cart_item
                        where cart_id = :cartId
                          and sku_id in (
                              select id from product_sku where sku_code = :skuCode
                          )
                        """)
                .param("cartId", cartId)
                .param("skuCode", skuCode)
                .update();
        touchCart(cartId);
    }

    public void clearCart(Long cartId) {
        jdbcClient.sql("""
                        delete from shopping_cart_item
                        where cart_id = :cartId
                        """)
                .param("cartId", cartId)
                .update();
        touchCart(cartId);
    }

    private void touchCart(Long cartId) {
        jdbcClient.sql("""
                        update shopping_cart
                        set updated_at = current_timestamp
                        where id = :cartId
                        """)
                .param("cartId", cartId)
                .update();
    }
}
