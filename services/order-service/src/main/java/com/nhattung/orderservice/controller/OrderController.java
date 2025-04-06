package com.nhattung.orderservice.controller;

import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.response.ApiResponse;
import com.nhattung.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;

    @PostMapping("/place-order")
    public ApiResponse<Order> placeOrder(@RequestBody List<Long> selectedCartItemIds) {
        Order order = orderService.placeOrder(selectedCartItemIds);
        return ApiResponse.<Order>builder()
                .message("Order placed successfully")
                .result(order)
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<OrderDto> getOrder(@PathVariable("orderId") Long orderId) {
        OrderDto order = orderService.getOrder(orderId);
        return ApiResponse.<OrderDto>builder()
                .message("Order retrieved successfully")
                .result(order)
                .build();
    }

    @GetMapping("/user-orders")
    public ApiResponse<List<OrderDto>> getUserOrders() {
        List<OrderDto> orders = orderService.getOrdersByUserId();
        return ApiResponse.<List<OrderDto>>builder()
                .message("User orders retrieved successfully")
                .result(orders)
                .build();
    }
}
