package com.malllite.order.controller;

import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.context.AuthContext;
import com.malllite.order.dto.CreateOrderRequest;
import com.malllite.order.dto.OrderDetailResponse;
import com.malllite.order.dto.OrderSummaryResponse;
import com.malllite.order.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequireAuth
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public OrderDetailResponse createOrder(@Valid @RequestBody CreateOrderRequest request) {
        return orderService.createOrder(AuthContext.getCurrentUser().userId(), request);
    }

    @GetMapping
    public List<OrderSummaryResponse> listOrders() {
        return orderService.listOrders(AuthContext.getCurrentUser().userId());
    }

    @GetMapping("/{orderId}")
    public OrderDetailResponse getOrderDetail(@PathVariable Long orderId) {
        return orderService.getOrderDetail(AuthContext.getCurrentUser().userId(), orderId);
    }
}
