package com.malllite.cart.dto;

import jakarta.validation.Valid;

import java.util.List;

public record SyncCartRequest(
        @Valid
        List<CartItemInput> items
) {
}
