package com.nhattung.cartservice.service.cartItem;

import com.nhattung.cartservice.dto.CartItemDto;
import com.nhattung.cartservice.entity.CartItem;
import com.nhattung.cartservice.request.AddItemToCartRequest;
import com.nhattung.cartservice.request.RemoveItemFromCartRequest;
import com.nhattung.cartservice.request.UpdateItemQuantityRequest;

import java.util.List;
import java.util.Set;

public interface ICartItemService {
    void addItemToCart(AddItemToCartRequest request);
    void removeItemFromCart(RemoveItemFromCartRequest request);
    void updateItemQuantity(UpdateItemQuantityRequest request);
    CartItem getCartItem(Long userId, Long productId);
    Set<CartItem> getCartItems(Long userId);
    Set<CartItemDto> getConvertedCartItems(List<CartItem> cartItems);
    CartItemDto convertToDto(CartItem cartItem);

}
