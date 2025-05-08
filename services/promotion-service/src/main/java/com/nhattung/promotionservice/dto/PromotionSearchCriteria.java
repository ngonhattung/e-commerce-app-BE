package com.nhattung.promotionservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PromotionSearchCriteria {
    private String searchTerm;
    private String promotionName;
    private String promotionCode;
    private boolean status;
    private LocalDate startDatePromotionStartDate;
    private LocalDate endDatePromotionStartDate;
    private LocalDate startDatePromotionEndDate;
    private LocalDate endDatePromotionEndDate;
}
