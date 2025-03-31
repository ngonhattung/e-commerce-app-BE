package com.nhattung.cartservice.service.cart;


import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.exception.AppException;
import com.nhattung.cartservice.exception.ErrorCode;
import com.nhattung.cartservice.repository.CartItemRepository;
import com.nhattung.cartservice.repository.CartRepository;
import com.nhattung.cartservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService implements ICartService{

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final AuthenticatedUser authenticatedUser;
    @Override
    public Cart getCart() {
        Cart cart = cartRepository.findByUserId(authenticatedUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));

        BigDecimal totalAmount = cart.getTotalAmount();
        cart.setTotalAmount(totalAmount);
        return cart;

    }

    @Transactional
    @Override
    public void clearCart() {
        Cart cart = cartRepository.findByUserId(authenticatedUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        cartItemRepository.deleteAll(cart.getItems());
        cart.clearCart();
        cartRepository.deleteById(cart.getId());
    }


    @Override
    public BigDecimal getTotalAmount() {
        Cart cart = cartRepository.findByUserId(authenticatedUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.CART_NOT_FOUND));
        return cart.getTotalAmount();
    }

    @Override
    public void initializeCart(String userId) {
        cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    return cartRepository.save(cart);
                });
    }
}
