package com.malllite.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record CreateProductSkuRequest(
        @NotBlank(message = "SKU code is required")
        String skuCode,
        @NotBlank(message = "SKU name is required")
        String name,
        @NotBlank(message = "SKU spec summary is required")
        String specSummary,
        @NotBlank(message = "SKU sale price is required")
        String salePrice,
        @NotBlank(message = "SKU market price is required")
        String marketPrice,
        @NotNull(message = "SKU stock is required")
        @PositiveOrZero(message = "SKU stock must be zero or greater")
        Integer stock,
        String coverImage,
        Boolean isDefault
) {
}
