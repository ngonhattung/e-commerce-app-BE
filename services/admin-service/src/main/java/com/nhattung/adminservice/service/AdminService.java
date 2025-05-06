package com.nhattung.adminservice.service;

import com.nhattung.adminservice.dto.*;
import com.nhattung.adminservice.repository.httpclient.OrderClient;
import com.nhattung.adminservice.repository.httpclient.ProductClient;
import com.nhattung.adminservice.repository.httpclient.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final OrderClient orderClient;
    private final ProductClient productClient;
    private final UserClient userClient;


    public SummaryDto getSummary() {
        var totalUsers = userClient.getTotalUserCount().getResult();
        var totalProducts = productClient.getTotalProductCount().getResult();
        var totalOrders = orderClient.getTotalOrderCount().getResult();
        var totalRevenue = orderClient.getTotalOrderRevenue().getResult();

        return SummaryDto.builder()
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .build();
    }

    public List<CategoryRevenueDto> getRevenueByCategory() {
        return orderClient.getRevenueByCategory().getResult();
    }

    public List<TopProductDto> getTopSellingProducts() {
        return orderClient.getTopSellingProducts().getResult();
    }

    public List<MonthlyRegistrationDto> getMonthlyRegistrationData() {
        return userClient.getMonthlyRegistrationData().getResult();
    }

    public List<RevenueDto> getRevenueByTimeRange(String timeRange) {
        return orderClient.getRevenueByTimeRange(timeRange).getResult();
    }
}
