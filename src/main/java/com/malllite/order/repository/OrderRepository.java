package com.malllite.order.repository;

import com.malllite.order.model.CustomerOrder;
import com.malllite.order.model.CustomerOrderItem;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class OrderRepository {

    private static final RowMapper<CustomerOrder> ORDER_ROW_MAPPER = (rs, rowNum) -> new CustomerOrder(
            rs.getLong("id"),
            rs.getString("order_no"),
            rs.getLong("user_id"),
            rs.getString("contact_name"),
            rs.getString("contact_phone"),
            rs.getString("shipping_address"),
            rs.getString("note"),
            rs.getString("payment_method"),
            rs.getString("status"),
            rs.getBigDecimal("subtotal").toPlainString(),
            rs.getBigDecimal("shipping_fee").toPlainString(),
            rs.getBigDecimal("total_amount").toPlainString(),
            rs.getTimestamp("created_at").toLocalDateTime()
    );

    private static final RowMapper<CustomerOrderItem> ORDER_ITEM_ROW_MAPPER = (rs, rowNum) -> new CustomerOrderItem(
            rs.getLong("id"),
            rs.getLong("order_id"),
            rs.getObject("product_id", Long.class),
            rs.getString("product_slug"),
            rs.getString("sku_code"),
            rs.getString("product_name"),
            rs.getString("sku_name"),
            rs.getString("cover_image"),
            rs.getBigDecimal("unit_price").toPlainString(),
            rs.getInt("quantity"),
            rs.getBigDecimal("line_total").toPlainString()
    );

    private final JdbcClient jdbcClient;

    public OrderRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public Long insertOrder(
            String orderNo,
            Long userId,
            String contactName,
            String contactPhone,
            String shippingAddress,
            String note,
            String paymentMethod,
            String status,
            String subtotal,
            String shippingFee,
            String totalAmount
    ) {
        return jdbcClient.sql("""
                        insert into customer_order (
                            order_no, user_id, contact_name, contact_phone, shipping_address, note,
                            payment_method, status, subtotal, shipping_fee, total_amount
                        )
                        values (
                            :orderNo, :userId, :contactName, :contactPhone, :shippingAddress, :note,
                            :paymentMethod, :status, cast(:subtotal as numeric), cast(:shippingFee as numeric), cast(:totalAmount as numeric)
                        )
                        returning id
                        """)
                .param("orderNo", orderNo)
                .param("userId", userId)
                .param("contactName", contactName)
                .param("contactPhone", contactPhone)
                .param("shippingAddress", shippingAddress)
                .param("note", note)
                .param("paymentMethod", paymentMethod)
                .param("status", status)
                .param("subtotal", subtotal)
                .param("shippingFee", shippingFee)
                .param("totalAmount", totalAmount)
                .query(Long.class)
                .single();
    }

    public void insertOrderItem(
            Long orderId,
            Long productId,
            String skuCode,
            String productName,
            String skuName,
            String coverImage,
            String unitPrice,
            Integer quantity,
            String lineTotal
    ) {
        jdbcClient.sql("""
                        insert into customer_order_item (
                            order_id, product_id, sku_code, product_name, sku_name, cover_image, unit_price, quantity, line_total
                        )
                        values (
                            :orderId, :productId, :skuCode, :productName, :skuName, :coverImage,
                            cast(:unitPrice as numeric), :quantity, cast(:lineTotal as numeric)
                        )
                        """)
                .param("orderId", orderId)
                .param("productId", productId)
                .param("skuCode", skuCode)
                .param("productName", productName)
                .param("skuName", skuName)
                .param("coverImage", coverImage)
                .param("unitPrice", unitPrice)
                .param("quantity", quantity)
                .param("lineTotal", lineTotal)
                .update();
    }

    public List<CustomerOrder> findOrdersByUserId(Long userId) {
        return jdbcClient.sql("""
                        select id, order_no, user_id, contact_name, contact_phone, shipping_address, note,
                               payment_method, status, subtotal, shipping_fee, total_amount, created_at
                        from customer_order
                        where user_id = :userId
                        order by created_at desc, id desc
                        """)
                .param("userId", userId)
                .query(ORDER_ROW_MAPPER)
                .list();
    }

    public Optional<CustomerOrder> findOrderByIdAndUserId(Long orderId, Long userId) {
        return jdbcClient.sql("""
                        select id, order_no, user_id, contact_name, contact_phone, shipping_address, note,
                               payment_method, status, subtotal, shipping_fee, total_amount, created_at
                        from customer_order
                        where id = :orderId and user_id = :userId
                        """)
                .param("orderId", orderId)
                .param("userId", userId)
                .query(ORDER_ROW_MAPPER)
                .optional();
    }

    public List<CustomerOrderItem> findItemsByOrderId(Long orderId) {
        return jdbcClient.sql("""
                        select oi.id, oi.order_id, oi.product_id, p.slug as product_slug, oi.sku_code, oi.product_name,
                               oi.sku_name, oi.cover_image, oi.unit_price, oi.quantity, oi.line_total
                        from customer_order_item oi
                        left join product p on p.id = oi.product_id
                        where oi.order_id = :orderId
                        order by oi.id asc
                        """)
                .param("orderId", orderId)
                .query(ORDER_ITEM_ROW_MAPPER)
                .list();
    }

    public Optional<Long> findProductIdBySlug(String slug) {
        return jdbcClient.sql("""
                        select id
                        from product
                        where slug = :slug
                        """)
                .param("slug", slug)
                .query(Long.class)
                .optional();
    }
}
