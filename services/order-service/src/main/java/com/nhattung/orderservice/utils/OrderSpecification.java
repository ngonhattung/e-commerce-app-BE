package com.nhattung.orderservice.utils;

import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.dto.OrderSearchCriteria;
import com.nhattung.orderservice.repository.httpclient.UserClient;
import jakarta.persistence.criteria.Path;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSpecification {

    private final UserClient userClient;

    //Search
    public Specification<OrderDto> withSearchCriteria(OrderSearchCriteria criteria) {
        return Specification
                .where(fieldContainsSearchTerm(criteria.getSearchTerm()));

    }

    //Lọc sản phẩm theo các tiêu chí khác nhau
    public Specification<OrderDto> withFilterCriteria(OrderSearchCriteria criteria) {
        return Specification
                .where(userNameContains(criteria.getCustomerName()))
                .and(userEmailContains(criteria.getCustomerEmail()))
                .and(userPhoneContains(criteria.getCustomerPhone()))
                .and(orderStatusEquals(criteria.getOrderStatus()))
                .and(priceGreaterThanOrEqual(criteria.getMinTotalPrice()))
                .and(priceLessThanOrEqual(criteria.getMaxTotalPrice()))
                .and(createdAtBetween(criteria.getStartDate(), criteria.getEndDate()));
    }

    private Specification<OrderDto> fieldContainsSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return null;
            }
            String likePattern = searchTerm.toLowerCase();
            List<String> matchedUserIds = userClient.findUserIdsBySearchTerm(likePattern).getResult();

            return criteriaBuilder.or(
                    root.get("userId").in(matchedUserIds),
                    criteriaBuilder.like(root.get("id").as(String.class), '%' + likePattern + '%')
            );
        };
    }


    //Lọc order theo trang thái
    private Specification<OrderDto> orderStatusEquals(String orderStatus) {
        return (root, query, criteriaBuilder) -> {
            if (orderStatus == null || orderStatus.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(root.get("orderStatus"), orderStatus);
        };
    }

    //Lọc order theo giá
    private Specification<OrderDto> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return (root, query, criteriaBuilder) -> {
            if (minPrice == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), minPrice);
        };
    }


    private Specification<OrderDto> priceLessThanOrEqual(BigDecimal maxPrice) {
        return (root, query, criteriaBuilder) -> {
            if (maxPrice == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), maxPrice);
        };
    }


    //Lọc order theo ngày tạo
    private Specification<OrderDto> createdAtBetween(LocalDate startDate, LocalDate endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null && endDate == null) return null;

            LocalDateTime start = (startDate != null) ? startDate.atStartOfDay() : null;
            LocalDateTime end = (endDate != null) ? endDate.atTime(LocalTime.MAX) : null;

            log.info(("orderDate: {}, start: {}, end: {}"), root.get("orderDate"), start, end);

            if (start != null && end != null) {
                return criteriaBuilder.between(root.get("orderDate"), start, end);
            } else if (start != null) {
                return criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), start);
            } else {
                return criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), end);
            }
        };
    }

    //Search tương đối theo tên người dùng
    private Specification<OrderDto> userNameContains(String userName) {
        return (root, query, criteriaBuilder) -> {
            if (userName == null || userName.isEmpty()) {
                return null;
            }

            List<String> matchedUserIds = userClient.findUserIdsBySearchTerm(userName).getResult();

            return criteriaBuilder.or(
                    root.get("userId").in(matchedUserIds)
            );
        };
    }

    //Search tương đối theo email người dùng
    private Specification<OrderDto> userEmailContains(String userEmail) {
        return (root, query, criteriaBuilder) -> {
            if (userEmail == null || userEmail.isEmpty()) {
                return null;
            }
            List<String> matchedUserIds = userClient.findUserIdsByEmail(userEmail).getResult();
            return criteriaBuilder.or(
                    root.get("userId").in(matchedUserIds)
            );
        };
    }

    //Search tương đối theo số điện thoại người dùng
    private Specification<OrderDto> userPhoneContains(String userPhone) {
        return (root, query, criteriaBuilder) -> {
            if (userPhone == null || userPhone.isEmpty()) {
                return null;
            }

            List<String> matchedUserIds = userClient.findUserIdsByPhone(userPhone).getResult();

            return criteriaBuilder.or(
                    root.get("userId").in(matchedUserIds)
            );
        };
    }

}
