package com.nhattung.cartservice.service.cartItem;

import com.nhattung.cartservice.dto.CartDto;
import com.nhattung.cartservice.dto.CartItemDto;
import com.nhattung.cartservice.dto.ProductDto;
import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.entity.CartItem;
import com.nhattung.cartservice.exception.AppException;
import com.nhattung.cartservice.exception.ErrorCode;
import com.nhattung.cartservice.repository.CartItemRepository;
import com.nhattung.cartservice.repository.CartRepository;
import com.nhattung.cartservice.repository.httpclient.ProductClient;
import com.nhattung.cartservice.request.AddItemToCartRequest;
import com.nhattung.cartservice.request.UpdateItemQuantityRequest;
import com.nhattung.cartservice.response.PageResponse;
import com.nhattung.cartservice.service.cart.CartService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CartItemService implements ICartItemService{

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;
    private final ProductClient productClient;
    private final CartRepository cartRepository;
    private final ModelMapper modelMapper;
    @Override
    public void addItemToCart(AddItemToCartRequest request) {
        //1. Get the cart
        //2. Get the product
        //3. Check if the product is already in the cart
        //4. If yes, update the quantity
        //5. If no, add the product to the cart



        Cart cart = cartService.getCart();

        if(cart.getItems().size() >= 20) {
            throw new AppException(ErrorCode.CART_ITEM_LIMIT);
        }
        ProductDto product = productClient.getProductById(request.getProductId()).getResult();
        if (product == null) {
            throw new AppException(ErrorCode.PRODUCT_NOT_FOUND);
        }

        if(product.getQuantity() < request.getQuantity()) {
            throw new AppException(ErrorCode.PRODUCT_NOT_ENOUGH);
        }
        CartItem cartItem = cart.getItems().stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .orElse(new CartItem());
        if (cartItem.getId() == null) {
            cartItem.setCart(cart);
            cartItem.setProductId(request.getProductId());
            cartItem.setUnitPrice(product.getSellingPrice());
            cartItem.setQuantity(request.getQuantity());
        }else {
            if(cartItem.getQuantity() + request.getQuantity() > product.getQuantity()) {
                throw new AppException(ErrorCode.PRODUCT_NOT_ENOUGH);
            }
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
        }

        cartItem.setTotalPrice();
        cartItemRepository.save(cartItem);
        cart.addItem(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void removeItemFromCart(Long productId) {
        Cart cart = cartService.getCart();
        CartItem cartItem = getCartItem(productId);
        cart.removeItem(cartItem);
        cartRepository.save(cart);
    }

    @Override
    public void updateItemQuantity(UpdateItemQuantityRequest request) {
        Cart cart = cartService.getCart();
        cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(request.getProductId()))
                .findFirst()
                .ifPresent(cartItem -> {
                    cartItem.setQuantity(request.getQuantity());
                    cartItem.setUnitPrice(productClient.getProductById(request.getProductId()).getResult().getSellingPrice());
                    cartItem.setTotalPrice();
                    cartItemRepository.save(cartItem);
                });
        BigDecimal totalAmount = cart.getItems()
                .stream()
                .map(CartItem::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        cart.setTotalAmount(totalAmount);
        cartRepository.save(cart);
    }

    @Override
    public CartItem getCartItem(Long productId) {
        Cart cart = cartService.getCart();
        return cart.getItems()
                .stream()
                .filter(item -> item.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new AppException(ErrorCode.CART_ITEM_NOT_FOUND));
    }

    @Override
    public Set<CartItem> getCartItems() {
        Cart cart = cartService.getCart();
        return cart.getItems();
    }

    @Override
    public PageResponse<CartItemDto> getPagedCartItems(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }
        Cart cart = cartService.getCart();
        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page-1,size, sort);
        Page<CartItem> cartItemsPage = cartItemRepository.findAllByCartId(cart.getId(), pageable);

        Set<CartItemDto> cartItemDtos = getConvertedCartItems(cartItemsPage.getContent());

        return PageResponse.<CartItemDto>builder()
                .currentPage(page)
                .totalPages(cartItemsPage.getTotalPages())
                .totalElements(cartItemsPage.getTotalElements())
                .pageSize(cartItemsPage.getSize())
                .data(cartItemDtos)
                .build();
    }

    @Override
    public Set<CartItemDto> getConvertedCartItems(List<CartItem> cartItems) {
        return cartItems.stream()
                .map(this::convertToDto)
                .collect(Collectors.toSet());
    }

    @Override
    public CartItemDto convertToDto(CartItem cartItem) {
        CartItemDto cartItemDto = modelMapper.map(cartItem, CartItemDto.class);
        ProductDto productDto = productClient.getProductById(cartItem.getProductId()).getResult();
        cartItemDto.setProduct(productDto);
        return cartItemDto;
    }

    @Override
    public CartDto convertToDto(Cart cart) {
        CartDto cartDto = modelMapper.map(cart, CartDto.class);
        cartDto.setItems(getConvertedCartItems(new ArrayList<>(cart.getItems())));
        return cartDto;
    }
}
