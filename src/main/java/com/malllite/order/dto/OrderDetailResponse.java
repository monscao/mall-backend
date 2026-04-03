package com.malllite.order.dto;

import java.util.List;

public record OrderDetailResponse(
        Long id,
        String orderNo,
        String status,
        String paymentMethod,
        String contactName,
        String contactPhone,
        String shippingAddress,
        String note,
        String subtotal,
        String shippingFee,
        String totalAmount,
        Integer totalQuantity,
        String createdAt,
        Boolean customerActionable,
        List<OrderItemResponse> items
) {
}
