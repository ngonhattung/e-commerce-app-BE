package com.nhattung.userservice.utils;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.dto.UserSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class UserSpecification {

    //Search
    public static Specification<UserProfileDto> withSearchCriteria(UserSearchCriteria criteria) {
        return Specification
                .where(fieldContainsSearchTerm(criteria.getSearchTerm()));

    }

    private static Specification<UserProfileDto> fieldContainsSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return null;
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(root.get("fullName").as(String.class), likePattern),
                    criteriaBuilder.like(root.get("email").as(String.class), likePattern),
                    criteriaBuilder.like(root.get("phone").as(String.class), likePattern)
            );
        };
    }
}
