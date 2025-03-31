package com.nhattung.cartservice.service.cartItem;

import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.entity.CartItem;
import com.nhattung.cartservice.repository.CartItemRepository;
import com.nhattung.cartservice.request.AddItemToCartRequest;
import com.nhattung.cartservice.request.RemoveItemFromCartRequest;
import com.nhattung.cartservice.request.UpdateItemQuantityRequest;
import com.nhattung.cartservice.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    @Override
    public void addItemToCart(AddItemToCartRequest request) {
        //1. Get the cart
        //2. Get the product
        //3. Check if the product is already in the cart
        //4. If yes, update the quantity
        //5. If no, add the product to the cart

        Cart cart = cartService.getCart(request.getUserId());

    }

    @Override
    public void removeItemFromCart(RemoveItemFromCartRequest request) {

    }

    @Override
    public void updateItemQuantity(UpdateItemQuantityRequest request) {

    }

    @Override
    public CartItem getCartItem(Long userId, Long productId) {
        return null;
    }
}
