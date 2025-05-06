package com.nhattung.orderservice.service;

import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.request.PageResponse;
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import org.springframework.data.domain.Page;

import java.util.List;

public interface IOrderService {

    Order placeOrder(SelectedCartItemRequest request);
    OrderDto getOrder(String orderId);
    List<OrderDto> getOrdersByUserId();
    OrderDto convertToDto(Order order);
    PageResponse<OrderDto> getAllOrders(int page, int size);
}
