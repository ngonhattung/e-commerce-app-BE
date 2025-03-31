package com.nhattung.cartservice.controller;

import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.response.ApiResponse;
import com.nhattung.cartservice.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.shaded.com.google.protobuf.Api;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    @GetMapping("/get")
    public ApiResponse<Cart> getCart() {
        Cart cart = cartService.getCart();
        return ApiResponse.<Cart>builder()
                .message("Cart retrieved successfully")
                .result(cart)
                .build();
    }

    @PostMapping("/initialize/{userId}")
    public ApiResponse<Void> initializeCart(@PathVariable String userId) {
        cartService.initializeCart(userId);
        return ApiResponse.<Void>builder()
                .message("Cart initialized successfully")
                .build();
    }

    @PostMapping("/clear")
    public ApiResponse<Void> clearCart() {
        cartService.clearCart();
        return ApiResponse.<Void>builder()
                .message("Cart cleared successfully")
                .build();
    }

    @GetMapping("/total-amount")
    public ApiResponse<BigDecimal> getTotalAmount() {
        BigDecimal totalAmount = cartService.getTotalAmount();
        return ApiResponse.<BigDecimal>builder()
                .message("Total amount retrieved successfully")
                .result(totalAmount)
                .build();
    }
}
