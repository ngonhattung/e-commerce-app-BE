package com.nhattung.cartservice.controller;


import com.nhattung.cartservice.dto.CartItemDto;
import com.nhattung.cartservice.entity.CartItem;
import com.nhattung.cartservice.request.AddItemToCartRequest;
import com.nhattung.cartservice.request.UpdateItemQuantityRequest;
import com.nhattung.cartservice.response.ApiResponse;
import com.nhattung.cartservice.response.PageResponse;
import com.nhattung.cartservice.service.cartItem.CartItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cartItems")
public class CartItemController {


    private final CartItemService cartItemService;

    @PostMapping("/add")
    public ApiResponse<Void> addItemToCart(@RequestBody AddItemToCartRequest request) {
        cartItemService.addItemToCart(request);
        return ApiResponse.<Void>builder()
                .message("Item added to cart successfully")
                .build();

    }

    @PostMapping("/update")
    public ApiResponse<Void> updateItemQuantity(@RequestBody UpdateItemQuantityRequest request) {
        cartItemService.updateItemQuantity(request);
        return ApiResponse.<Void>builder()
                .message("Item quantity updated successfully")
                .build();
    }

    @PostMapping("/remove/{productId}")
    public ApiResponse<Void> removeItemFromCart(@PathVariable("productId") Long productId) {
        cartItemService.removeItemFromCart(productId);
        return ApiResponse.<Void>builder()
                .message("Item removed from cart successfully")
                .build();
    }


    @GetMapping("/getAll")
    public ApiResponse<PageResponse<CartItemDto>> getCartItems(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<CartItemDto>>builder()
                .message("Cart item retrieved successfully")
                .result(cartItemService.getPagedCartItems(page, size))
                .build();
    }


    @PostMapping("/deleteItemsOrder")
    public ApiResponse<Void> deleteItemsOrder(@RequestBody List<Long> itemIds) {
        cartItemService.deleteItemOrderFromCart(itemIds);
        return ApiResponse.<Void>builder()
                .message("Items deleted from cart successfully")
                .build();
    }

}
