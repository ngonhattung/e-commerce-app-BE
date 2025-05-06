package com.nhattung.adminservice.repository.httpclient;

import com.nhattung.adminservice.config.AuthRequestInterceptor;
import com.nhattung.adminservice.dto.CategoryRevenueDto;
import com.nhattung.adminservice.dto.RevenueDto;
import com.nhattung.adminservice.dto.TopProductDto;
import com.nhattung.adminservice.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@FeignClient(name = "order-service", configuration = AuthRequestInterceptor.class)
public interface OrderClient {


    @GetMapping(value = "/orders/count", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<Long> getTotalOrderCount();

    @GetMapping(value = "/orders/revenue", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<BigDecimal> getTotalOrderRevenue();

    @GetMapping(value = "/orders/revenue-by-category", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<CategoryRevenueDto>>  getRevenueByCategory();

    @GetMapping(value = "/orders/top-selling-products", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<TopProductDto>> getTopSellingProducts();

    @GetMapping(value = "/orders/revenue-by-time-range", produces = MediaType.APPLICATION_JSON_VALUE)
    ApiResponse<List<RevenueDto>> getRevenueByTimeRange(@RequestParam("timeRange") String timeRange);
}
