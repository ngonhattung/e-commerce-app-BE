package com.nhattung.cartservice.service.cartItem;

import com.nhattung.cartservice.dto.CartDto;
import com.nhattung.cartservice.dto.CartItemDto;
import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.entity.CartItem;
import com.nhattung.cartservice.request.AddItemToCartRequest;
import com.nhattung.cartservice.request.UpdateItemQuantityRequest;
import com.nhattung.cartservice.response.PageResponse;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface ICartItemService {
    void addItemToCart(AddItemToCartRequest request);
    void removeItemFromCart(Long productId);
    void updateItemQuantity(UpdateItemQuantityRequest request);
    CartItem getCartItem(Long productId);
    Set<CartItem> getCartItems();
    PageResponse<CartItemDto> getPagedCartItems(int page, int size);
    Set<CartItemDto> getConvertedCartItems(List<CartItem> cartItems);
    CartItemDto convertToDto(CartItem cartItem);
    CartDto convertToDto(Cart cart);
    void deleteItemOrderFromCart(List<Long> itemIds);

}
