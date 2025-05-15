package com.nhattung.orderservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PromotionDto {
    private Long id;
    private String promotionName;
    private String description;
    private String couponCode;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minimumOrderValue;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean isActive;
    private Boolean isGlobal;
}
