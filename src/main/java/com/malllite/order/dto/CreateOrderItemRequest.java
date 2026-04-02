package com.malllite.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderItemRequest(
        String productSlug,
        @NotBlank(message = "Item SKU code is required")
        String skuCode,
        @NotBlank(message = "Item product name is required")
        String productName,
        String skuName,
        String coverImage,
        @NotBlank(message = "Item sale price is required")
        String salePrice,
        @NotNull(message = "Item quantity is required")
        @Positive(message = "Item quantity must be greater than zero")
        Integer quantity
) {
}
