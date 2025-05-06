package com.nhattung.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private String id;
    private UserProfileDto user;
    private LocalDateTime orderDate;
    private BigDecimal totalAmount;;
    private String status;
    private String shippingAddress;
    private List<OrderItemDto> items;
}
