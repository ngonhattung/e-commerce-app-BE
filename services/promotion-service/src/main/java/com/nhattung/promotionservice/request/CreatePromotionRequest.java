package com.nhattung.promotionservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreatePromotionRequest {
    private String promotionName;
    private String description;
    private String couponCode;
    private BigDecimal discountPercent;
    private BigDecimal discountAmount;
    private BigDecimal minimumOrderValue;
    private Instant startDate;
    private Instant endDate;
    private Boolean isActive;
}
