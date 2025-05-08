package com.nhattung.promotionservice.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePromotionRequest {
    @NotBlank(message = "EMPTY_PROMOTION_NAME")
    private String promotionName;
    private String description;
    @NotBlank(message = "EMPTY_COUPON_CODE")
    private String couponCode;
    @NotNull(message = "EMPTY_DISCOUNT_PERCENT")
    private BigDecimal discountPercent;
    @NotNull(message = "EMPTY_DISCOUNT_AMOUNT")
    private BigDecimal discountAmount;
    @NotNull(message = "EMPTY_MINIMUM_ORDER_VALUE")
    private BigDecimal minimumOrderValue;
    @NotNull(message = "EMPTY_START_DATE")
    private LocalDateTime startDate;
    @NotNull(message = "EMPTY_END_DATE")
    private LocalDateTime endDate;
    @Builder.Default
    private Boolean isActive = true;
    @Builder.Default
    private Boolean isGlobal = true;
}
