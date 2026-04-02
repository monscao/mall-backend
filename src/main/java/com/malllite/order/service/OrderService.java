package com.malllite.order.service;

import com.malllite.order.dto.CreateOrderItemRequest;
import com.malllite.order.dto.CreateOrderRequest;
import com.malllite.order.dto.OrderDetailResponse;
import com.malllite.order.dto.OrderItemResponse;
import com.malllite.order.dto.OrderSummaryResponse;
import com.malllite.order.model.CustomerOrder;
import com.malllite.order.model.CustomerOrderItem;
import com.malllite.order.repository.OrderRepository;
import com.malllite.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
public class OrderService {

    private static final DateTimeFormatter ORDER_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public OrderDetailResponse createOrder(Long userId, CreateOrderRequest request) {
        String orderNo = "ML" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
        int totalQuantity = request.items().stream().mapToInt(item -> item.quantity() == null ? 0 : item.quantity()).sum();
        String subtotal = request.items().stream()
                .map(item -> toLineTotal(item.salePrice(), item.quantity()))
                .reduce(0.0, Double::sum)
                .toString();
        String shippingFee = request.shippingFee() == null || request.shippingFee().isBlank() ? "0" : request.shippingFee();
        String totalAmount = String.valueOf(Double.parseDouble(subtotal) + Double.parseDouble(shippingFee));

        Long orderId = orderRepository.insertOrder(
                orderNo,
                userId,
                request.contactName(),
                request.contactPhone(),
                request.shippingAddress(),
                request.note(),
                request.paymentMethod(),
                "PAID",
                subtotal,
                shippingFee,
                totalAmount
        );

        for (CreateOrderItemRequest item : request.items()) {
            Long productId = item.productSlug() == null ? null : orderRepository.findProductIdBySlug(item.productSlug()).orElse(null);
            orderRepository.insertOrderItem(
                    orderId,
                    productId,
                    item.skuCode(),
                    item.productName(),
                    item.skuName(),
                    item.coverImage(),
                    item.salePrice(),
                    item.quantity(),
                    String.valueOf(toLineTotal(item.salePrice(), item.quantity()))
            );
        }

        return getOrderDetail(userId, orderId);
    }

    public List<OrderSummaryResponse> listOrders(Long userId) {
        return orderRepository.findOrdersByUserId(userId)
                .stream()
                .map(order -> new OrderSummaryResponse(
                        order.id(),
                        order.orderNo(),
                        order.status(),
                        order.paymentMethod(),
                        order.subtotal(),
                        order.shippingFee(),
                        order.totalAmount(),
                        orderRepository.findItemsByOrderId(order.id()).stream().mapToInt(CustomerOrderItem::quantity).sum(),
                        order.createdAt().format(ORDER_TIME_FORMAT)
                ))
                .toList();
    }

    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
        CustomerOrder order = orderRepository.findOrderByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<OrderItemResponse> items = orderRepository.findItemsByOrderId(orderId)
                .stream()
                .map(item -> new OrderItemResponse(
                        item.id(),
                        item.productSlug(),
                        item.skuCode(),
                        item.productName(),
                        item.skuName(),
                        item.coverImage(),
                        item.unitPrice(),
                        item.quantity(),
                        item.lineTotal()
                ))
                .toList();

        int totalQuantity = items.stream().mapToInt(OrderItemResponse::quantity).sum();

        return new OrderDetailResponse(
                order.id(),
                order.orderNo(),
                order.status(),
                order.paymentMethod(),
                order.contactName(),
                order.contactPhone(),
                order.shippingAddress(),
                order.note(),
                order.subtotal(),
                order.shippingFee(),
                order.totalAmount(),
                totalQuantity,
                order.createdAt().format(ORDER_TIME_FORMAT),
                items
        );
    }

    private double toLineTotal(String salePrice, Integer quantity) {
        return Double.parseDouble(salePrice) * quantity;
    }
}
