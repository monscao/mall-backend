package com.malllite.cart.controller;

import com.malllite.auth.PermissionCodes;
import com.malllite.auth.annotation.RequireAuth;
import com.malllite.auth.annotation.RequirePermission;
import com.malllite.auth.context.AuthContext;
import com.malllite.cart.dto.CartResponse;
import com.malllite.cart.dto.SyncCartRequest;
import com.malllite.cart.service.CartService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cart")
@RequireAuth
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    @RequirePermission(PermissionCodes.CART_READ)
    public CartResponse getCart() {
        return cartService.getCart(AuthContext.getCurrentUser().userId());
    }

    @PutMapping
    @RequirePermission(PermissionCodes.CART_WRITE)
    public CartResponse syncCart(@Valid @RequestBody SyncCartRequest request) {
        return cartService.syncCart(AuthContext.getCurrentUser().userId(), request);
    }

    @DeleteMapping
    @RequirePermission(PermissionCodes.CART_WRITE)
    public CartResponse clearCart() {
        return cartService.clearCart(AuthContext.getCurrentUser().userId());
    }
}
