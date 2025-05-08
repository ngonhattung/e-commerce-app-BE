package com.nhattung.promotionservice.utils;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.dto.PromotionSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class PromotionSpecification {

    //Search
    public static Specification<PromotionDto> withSearchCriteria(PromotionSearchCriteria criteria) {
        return Specification
                .where(fieldContainsSearchTerm(criteria.getSearchTerm()));

    }
    //Lọc sản phẩm theo các tiêu chí khác nhau
    public static Specification<PromotionDto> withFilterCriteria(PromotionSearchCriteria criteria) {
        return Specification
                .where(promotionNameContains(criteria.getPromotionName()))
                .and(promotionCodeContains(criteria.getPromotionCode()))
                .and(promotionStatusEquals(criteria.isStatus()))
                .and(startDateBetween(criteria.getStartDatePromotionStartDate(), criteria.getEndDatePromotionStartDate()))
                .and(endDateBetween(criteria.getStartDatePromotionEndDate(), criteria.getEndDatePromotionEndDate()));
    }

    private static Specification<PromotionDto> fieldContainsSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return null;
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("promotionName")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("couponCode")), likePattern),
                    criteriaBuilder.like(root.get("id").as(String.class), likePattern)
            );
        };
    }

    private static Specification<PromotionDto> promotionNameContains(String promotionName) {
        return (root, query, criteriaBuilder) -> {
            if (promotionName == null || promotionName.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("promotionName")),
                    "%" + promotionName.toLowerCase() + "%"
            );
        };
    }

    private static Specification<PromotionDto> promotionCodeContains(String promotionCode) {
        return (root, query, criteriaBuilder) -> {
            if (promotionCode == null || promotionCode.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("couponCode")),
                    "%" + promotionCode.toLowerCase() + "%"
            );
        };
    }

    private static Specification<PromotionDto> promotionStatusEquals(boolean promotionStatus) {
        return (root, query, criteriaBuilder) -> {
            if (promotionStatus) {
                return criteriaBuilder.equal(root.get("isActive"), true);
            } else {
                return criteriaBuilder.equal(root.get("isActive"), false);
            }
        };
    }


    //Lọc promotion theo ngày bắt đầu
    private static Specification<PromotionDto> startDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null;
            }

            LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
            LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("startDate"), start, end);
            } else if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("startDate"), start);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), end);
            }
        };
    }

    //Lọc promotion theo ngày kết thúc
    private static Specification<PromotionDto> endDateBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) {
                return null;
            }

            LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
            LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("endDate"), start, end);
            } else if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("endDate"), start);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("endDate"), end);
            }
        };
    }
}
