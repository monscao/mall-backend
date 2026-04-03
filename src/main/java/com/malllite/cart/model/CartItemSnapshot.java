package com.malllite.cart.model;

public record CartItemSnapshot(
        Long id,
        Long cartId,
        Long productId,
        Long skuId,
        String productSlug,
        String productName,
        String skuCode,
        String skuName,
        String specSummary,
        String coverImage,
        String salePrice,
        String marketPrice,
        Integer stock,
        Integer quantity
) {
}
