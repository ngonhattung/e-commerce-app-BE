package com.nhattung.cartservice.service.cart;

import com.nhattung.cartservice.entity.Cart;

import java.math.BigDecimal;

public interface ICartService {

    Cart getCart(Long userId);
    void clearCart(Long userId);
    BigDecimal getTotalAmount(Long userId);
    void initializeCart(Long userId);
}
