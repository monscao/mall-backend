package com.malllite.cart.dto;

import java.util.List;

public record CartResponse(
        Long cartId,
        Integer totalItems,
        String subtotal,
        List<CartItemResponse> items
) {
}
