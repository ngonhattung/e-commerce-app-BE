package com.nhattung.adminservice.controller;

import com.nhattung.adminservice.dto.*;
import com.nhattung.adminservice.response.ApiResponse;
import com.nhattung.adminservice.service.AdminService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {


    private final AdminService adminService;

    @GetMapping("/summary")
    public ApiResponse<SummaryDto> getSummary() {
        SummaryDto summary = adminService.getSummary();
        return ApiResponse.<SummaryDto>builder()
                .message("Get summary successfully")
                .result(summary)
                .build();
    }

    @GetMapping("/revenue-by-category")
    public ApiResponse<List<CategoryRevenueDto>> getRevenueByCategory() {
        return ApiResponse.<List<CategoryRevenueDto>>builder()
                .message("Get revenue by category successfully")
                .result(adminService.getRevenueByCategory())
                .build();
    }

    @GetMapping("/top-selling-products")
    public ApiResponse<List<TopProductDto>> getTopSellingProducts() {
        return ApiResponse.<List<TopProductDto>>builder()
                .message("Get top selling products successfully")
                .result(adminService.getTopSellingProducts())
                .build();
    }

    @GetMapping("/monthly-registration")
    public ApiResponse<List<MonthlyRegistrationDto>> getMonthlyRegistrationData() {
        return ApiResponse.<List<MonthlyRegistrationDto>>builder()
                .message("Get monthly registration data successfully")
                .result(adminService.getMonthlyRegistrationData())
                .build();
    }


    @GetMapping("/revenue-by-time-range")
    public ApiResponse<List<RevenueDto>> getRevenueByTimeRange(@RequestParam("timeRange") String timeRange) {
        return ApiResponse.<List<RevenueDto>>builder()
                .message("Get revenue by time range successfully")
                .result(adminService.getRevenueByTimeRange(timeRange))
                .build();
    }

    @GetMapping("/order-status-stats")
    public ApiResponse<List<OrderStatusStatsDto>> getOrderStatusStats() {
        return ApiResponse.<List<OrderStatusStatsDto>>builder()
                .message("Get order status statistics successfully")
                .result(adminService.getOrderStatusStats())
                .build();
    }

    @GetMapping("/monthly-order-stats")
    public ApiResponse<List<MonthlyOrderStatsDto>> getMonthlyOrderStats() {
        return ApiResponse.<List<MonthlyOrderStatsDto>>builder()
                .message("Get monthly order statistics successfully")
                .result(adminService.getMonthlyOrderStats())
                .build();
    }

}
