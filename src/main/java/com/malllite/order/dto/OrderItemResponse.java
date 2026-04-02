package com.malllite.order.dto;

public record OrderItemResponse(
        Long id,
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
