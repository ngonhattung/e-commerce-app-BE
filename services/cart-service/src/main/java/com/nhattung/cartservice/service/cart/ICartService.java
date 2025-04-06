package com.nhattung.cartservice.service.cart;

import com.nhattung.cartservice.dto.CartDto;
import com.nhattung.cartservice.entity.Cart;

import java.math.BigDecimal;

public interface ICartService {

    Cart getCart();
    void clearCart();
    BigDecimal getTotalAmount();
    void initializeCart(String userId);
    //CartDto convertToDto(Cart cart);
}
