package com.malllite.order.model;

import java.time.LocalDateTime;

public record CustomerOrder(
        Long id,
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
        String totalAmount,
        LocalDateTime createdAt
) {
}
