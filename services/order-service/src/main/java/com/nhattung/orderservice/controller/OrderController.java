package com.nhattung.orderservice.controller;

import com.nhattung.orderservice.dto.*;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.request.PageResponse;
import com.nhattung.orderservice.request.SelectedCartItemRequest;
import com.nhattung.orderservice.response.ApiResponse;
import com.nhattung.orderservice.service.IOrderService;
import com.nhattung.orderservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final IOrderService orderService;
    private final AuthenticatedUser authenticatedUser;
    @PostMapping("/place-order")
    public ApiResponse<Order> placeOrder(@RequestBody SelectedCartItemRequest request) {
        Order order = orderService.placeOrder(request);
        return ApiResponse.<Order>builder()
                .message("Order created successfully with ID: " + order.getId())
                .result(order)
                .build();
    }

    @GetMapping("/order/{orderId}")
    public ApiResponse<OrderDto> getOrder(@PathVariable("orderId") String orderId) {
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/getAll")
    public ApiResponse<PageResponse<OrderDto>> getAllOrders(
            @RequestParam (value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ApiResponse.<PageResponse<OrderDto>>builder()
                .message("All orders retrieved successfully")
                .result(orderService.getAllOrders(page, size))
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/count")
    public ApiResponse<Long> countOrders() {
        long count = orderService.countOrders();
        return ApiResponse.<Long>builder()
                .message("Total orders count retrieved successfully")
                .result(count)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/revenue")
    public ApiResponse<BigDecimal> getTotalRevenue() {
        BigDecimal totalRevenue = orderService.getTotalRevenue();
        return ApiResponse.<BigDecimal>builder()
                .message("Total revenue retrieved successfully")
                .result(totalRevenue)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/revenue-by-time-range")
    public ApiResponse<List<RevenueDto>> getRevenueByTimeRange(@RequestParam("timeRange") String timeRange) {
        List<RevenueDto> revenue = orderService.getRevenueByTimeRange(timeRange);
        return ApiResponse.<List<RevenueDto>>builder()
                .message("Revenue by time range retrieved successfully")
                .result(revenue)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/revenue-by-category")
    public ApiResponse<List<CategoryRevenueDto>> getRevenueByCategory() {
        return ApiResponse.<List<CategoryRevenueDto>>builder()
                .message("Revenue by category retrieved successfully")
                .result(orderService.getRevenueByCategory())
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/top-selling-products")
    public ApiResponse<List<TopProductDto>> getTopSellingProducts() {
        return ApiResponse.<List<TopProductDto>>builder()
                .message("Top selling products retrieved successfully")
                .result(orderService.getTopSellingProducts())
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/order-status-stats")
    public ApiResponse<List<OrderStatusStatsDto>> getOrderStatusStats() {
        return ApiResponse.<List<OrderStatusStatsDto>>builder()
                .message("Order status statistics retrieved successfully")
                .result(orderService.getOrderStatusStats())
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/monthly-order-stats")
    public ApiResponse<List<MonthlyOrderStatsDto>> getMonthlyOrderStats() {
        return ApiResponse.<List<MonthlyOrderStatsDto>>builder()
                .message("Monthly order statistics retrieved successfully")
                .result(orderService.getMonthlyOrderStats())
                .build();
    }

}
