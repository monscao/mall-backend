package com.malllite.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CreateOrderRequest(
        @NotBlank(message = "Contact name is required")
        String contactName,
        @NotBlank(message = "Contact phone is required")
        String contactPhone,
        @NotBlank(message = "Shipping address is required")
        String shippingAddress,
        String note,
        @NotBlank(message = "Payment method is required")
        String paymentMethod,
        String shippingFee,
        @NotEmpty(message = "Order items are required")
        @Valid
        List<CreateOrderItemRequest> items
) {
}
