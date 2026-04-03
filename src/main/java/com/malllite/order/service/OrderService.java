package com.malllite.order.service;

import com.malllite.order.dto.CreateOrderItemRequest;
import com.malllite.order.dto.CreateOrderRequest;
import com.malllite.order.dto.OrderDetailResponse;
import com.malllite.order.dto.OrderItemResponse;
import com.malllite.order.dto.OrderSummaryResponse;
import com.malllite.order.model.OrderStatus;
import com.malllite.order.model.CustomerOrder;
import com.malllite.order.model.CustomerOrderItem;
import com.malllite.order.repository.OrderRepository;
import com.malllite.common.exception.BadRequestException;
import com.malllite.common.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
        BigDecimal subtotal = request.items().stream()
                .map(item -> toLineTotal(item.salePrice(), item.quantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal shippingFee = parseMoney(request.shippingFee());
        BigDecimal totalAmount = subtotal.add(shippingFee);
        String initialStatus = resolveInitialStatus(request.paymentMethod()).name();

        Long orderId = orderRepository.insertOrder(
                orderNo,
                userId,
                request.contactName(),
                request.contactPhone(),
                request.shippingAddress(),
                request.note(),
                request.paymentMethod(),
                initialStatus,
                subtotal.toPlainString(),
                shippingFee.toPlainString(),
                totalAmount.toPlainString()
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
                    toLineTotal(item.salePrice(), item.quantity()).toPlainString()
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
                        order.createdAt().format(ORDER_TIME_FORMAT),
                        canCustomerManage(order.status())
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
                canCustomerManage(order.status()),
                items
        );
    }

    public List<OrderSummaryResponse> listAllOrders() {
        return orderRepository.findAllOrders().stream()
                .map(order -> new OrderSummaryResponse(
                        order.id(),
                        order.orderNo(),
                        order.status(),
                        order.paymentMethod(),
                        order.subtotal(),
                        order.shippingFee(),
                        order.totalAmount(),
                        orderRepository.findItemsByOrderId(order.id()).stream().mapToInt(CustomerOrderItem::quantity).sum(),
                        order.createdAt().format(ORDER_TIME_FORMAT),
                        canCustomerManage(order.status())
                ))
                .toList();
    }

    public OrderDetailResponse getOrderDetailForAdmin(Long orderId) {
        CustomerOrder order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        return buildOrderDetail(order);
    }

    public OrderDetailResponse updateOrderStatus(Long orderId, String targetStatus, boolean adminAction) {
        CustomerOrder order = orderRepository.findOrderById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        OrderStatus current = parseStatus(order.status());
        OrderStatus next = parseStatus(targetStatus);
        if (!isTransitionAllowed(current, next, adminAction)) {
            throw new BadRequestException("Order status transition is not allowed");
        }

        orderRepository.updateOrderStatus(orderId, next.name());
        return getOrderDetailForAdmin(orderId);
    }

    public OrderDetailResponse cancelOrder(Long userId, Long orderId) {
        CustomerOrder order = orderRepository.findOrderByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (!canCustomerManage(order.status())) {
            throw new BadRequestException("Order can no longer be cancelled");
        }

        orderRepository.updateOrderStatus(orderId, OrderStatus.CANCELLED.name());
        return getOrderDetail(userId, orderId);
    }

    private OrderDetailResponse buildOrderDetail(CustomerOrder order) {
        List<OrderItemResponse> items = orderRepository.findItemsByOrderId(order.id())
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
                canCustomerManage(order.status()),
                items
        );
    }

    private BigDecimal parseMoney(String value) {
        if (value == null || value.isBlank()) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(value);
    }

    private BigDecimal toLineTotal(String salePrice, Integer quantity) {
        return new BigDecimal(salePrice).multiply(BigDecimal.valueOf(quantity));
    }

    private OrderStatus resolveInitialStatus(String paymentMethod) {
        return "cod".equalsIgnoreCase(paymentMethod) ? OrderStatus.PENDING_PAYMENT : OrderStatus.PAID;
    }

    private OrderStatus parseStatus(String status) {
        try {
            return OrderStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new BadRequestException("Unsupported order status");
        }
    }

    private boolean canCustomerManage(String status) {
        OrderStatus current = parseStatus(status);
        return current == OrderStatus.PENDING_PAYMENT || current == OrderStatus.PAID || current == OrderStatus.PROCESSING;
    }

    private boolean isTransitionAllowed(OrderStatus current, OrderStatus next, boolean adminAction) {
        if (current == next) {
            return true;
        }

        if (!adminAction) {
            return next == OrderStatus.CANCELLED && canCustomerManage(current.name());
        }

        return switch (current) {
            case PENDING_PAYMENT -> next == OrderStatus.PAID || next == OrderStatus.CANCELLED;
            case PAID -> next == OrderStatus.PROCESSING || next == OrderStatus.CANCELLED;
            case PROCESSING -> next == OrderStatus.SHIPPED || next == OrderStatus.CANCELLED;
            case SHIPPED -> next == OrderStatus.COMPLETED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}
