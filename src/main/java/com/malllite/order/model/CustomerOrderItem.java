package com.malllite.order.model;

public record CustomerOrderItem(
        Long id,
        Long orderId,
        Long productId,
        String productSlug,
        String skuCode,
        String productName,
        String skuName,
        String coverImage,
        String unitPrice,
        Integer quantity,
        String lineTotal
) {
}
