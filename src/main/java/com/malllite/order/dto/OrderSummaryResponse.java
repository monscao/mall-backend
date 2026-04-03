package com.malllite.order.dto;

public record OrderSummaryResponse(
        Long id,
        String orderNo,
        String status,
        String paymentMethod,
        String subtotal,
        String shippingFee,
        String totalAmount,
        Integer totalQuantity,
        String createdAt,
        Boolean customerActionable
) {
}
