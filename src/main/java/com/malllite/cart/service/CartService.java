package com.malllite.cart.service;

import com.malllite.cart.dto.CartItemInput;
import com.malllite.cart.dto.CartItemResponse;
import com.malllite.cart.dto.CartResponse;
import com.malllite.cart.dto.SyncCartRequest;
import com.malllite.cart.model.CartItemSnapshot;
import com.malllite.cart.repository.CartRepository;
import com.malllite.common.exception.BadRequestException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    public CartResponse getCart(Long userId) {
        Long cartId = cartRepository.findOrCreateCartId(userId);
        return toCartResponse(cartId, cartRepository.findItemsByCartId(cartId));
    }

    public CartResponse syncCart(Long userId, SyncCartRequest request) {
        Long cartId = cartRepository.findOrCreateCartId(userId);
        List<CartItemSnapshot> resolvedItems = new ArrayList<>();

        for (CartItemInput item : request.items() == null ? List.<CartItemInput>of() : request.items()) {
            resolvedItems.add(resolveItem(item));
        }

        cartRepository.replaceItems(cartId, mergeDuplicateItems(resolvedItems));
        return getCart(userId);
    }

    public CartResponse clearCart(Long userId) {
        Long cartId = cartRepository.findOrCreateCartId(userId);
        cartRepository.clearCart(cartId);
        return getCart(userId);
    }

    private CartItemSnapshot resolveItem(CartItemInput item) {
        CartItemSnapshot snapshot = cartRepository.findSkuSnapshotBySkuCode(item.skuCode())
                .orElseThrow(() -> new BadRequestException("Cart item SKU is invalid"));

        if (snapshot.stock() <= 0) {
            throw new BadRequestException("Cart item is out of stock");
        }

        int quantity = Math.min(item.quantity(), snapshot.stock());
        return new CartItemSnapshot(
                snapshot.id(),
                snapshot.cartId(),
                snapshot.productId(),
                snapshot.skuId(),
                snapshot.productSlug(),
                snapshot.productName(),
                snapshot.skuCode(),
                snapshot.skuName(),
                snapshot.specSummary(),
                snapshot.coverImage(),
                snapshot.salePrice(),
                snapshot.marketPrice(),
                snapshot.stock(),
                quantity
        );
    }

    private List<CartItemSnapshot> mergeDuplicateItems(List<CartItemSnapshot> items) {
        List<CartItemSnapshot> merged = new ArrayList<>();

        for (CartItemSnapshot item : items) {
            int existingIndex = -1;
            for (int index = 0; index < merged.size(); index++) {
                if (merged.get(index).skuCode().equals(item.skuCode())) {
                    existingIndex = index;
                    break;
                }
            }

            if (existingIndex < 0) {
                merged.add(item);
                continue;
            }

            CartItemSnapshot existing = merged.get(existingIndex);
            int quantity = Math.min(existing.quantity() + item.quantity(), existing.stock());
            merged.set(existingIndex, new CartItemSnapshot(
                    existing.id(),
                    existing.cartId(),
                    existing.productId(),
                    existing.skuId(),
                    existing.productSlug(),
                    existing.productName(),
                    existing.skuCode(),
                    existing.skuName(),
                    existing.specSummary(),
                    existing.coverImage(),
                    existing.salePrice(),
                    existing.marketPrice(),
                    existing.stock(),
                    quantity
            ));
        }

        return merged;
    }

    private CartResponse toCartResponse(Long cartId, List<CartItemSnapshot> items) {
        int totalItems = items.stream().mapToInt(CartItemSnapshot::quantity).sum();
        BigDecimal subtotal = items.stream()
                .map(item -> new BigDecimal(item.salePrice()).multiply(BigDecimal.valueOf(item.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new CartResponse(
                cartId,
                totalItems,
                subtotal.toPlainString(),
                items.stream()
                        .map(item -> new CartItemResponse(
                                item.id(),
                                item.productSlug(),
                                item.productName(),
                                item.skuCode(),
                                item.skuName(),
                                item.specSummary(),
                                item.coverImage(),
                                item.salePrice(),
                                item.marketPrice(),
                                item.stock(),
                                item.quantity(),
                                new BigDecimal(item.salePrice()).multiply(BigDecimal.valueOf(item.quantity())).toPlainString()
                        ))
                        .toList()
        );
    }
}
