package com.malllite.cart.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CartItemInput(
        @NotBlank(message = "Cart item SKU code is required")
        String skuCode,
        @NotNull(message = "Cart item quantity is required")
        @Positive(message = "Cart item quantity must be greater than zero")
        Integer quantity
) {
}
