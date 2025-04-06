package com.nhattung.orderservice.service;


import com.nhattung.orderservice.dto.CartDto;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.repository.httpclient.CartClient;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService implements IOrderService{

    private final OrderRepository orderRepository;
    private final AuthenticatedUser authenticatedUser;
    private final CartClient cartClient;

    @Override
    public Order placeOrder(List<Long> selectedCartItemIds) {
        CartDto cart = cartClient.getCart();
        var cartItems = cart.getItems()
                .stream()
                .filter(cartItem -> selectedCartItemIds.contains(cartItem.getId()))
                .toList();
        if(cartItems.isEmpty()){
            throw new RuntimeException("No cart items selected");
        }

        return null;
    }

    @Override
    public Order getOrder(Long orderId) {
        return null;
    }

    @Override
    public List<Order> getOrdersByUserId() {
        return List.of();
    }

    @Override
    public OrderDto convertToDto(Order order) {
        return null;
    }
}
