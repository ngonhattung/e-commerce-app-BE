package com.nhattung.orderservice.service;

import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;

import java.util.List;

public interface IOrderService {

    Order placeOrder(List<Long> selectedCartItemIds);
    Order getOrder(Long orderId);
    List<Order> getOrdersByUserId();
    OrderDto convertToDto(Order order);
}
