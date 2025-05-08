package com.nhattung.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderSearchCriteria {
    private String searchTerm;
    private String customerName;
    private String customerEmail;
    private String customerPhone;
    private String orderStatus;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal minTotalPrice;
    private BigDecimal maxTotalPrice;
}
