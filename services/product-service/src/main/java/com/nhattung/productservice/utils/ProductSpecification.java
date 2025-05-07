package com.nhattung.productservice.utils;

import com.nhattung.productservice.dto.ProductDto;
import com.nhattung.productservice.dto.ProductSearchCriteria;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class ProductSpecification {


    //Search admin
    public static Specification<ProductDto> withSearchCriteria(ProductSearchCriteria criteria) {
        return Specification
                .where(fieldContainsSearchTerm(criteria.getSearchTerm()));

    }


    //Lọc sản phẩm theo các tiêu chí khác nhau (admin)
    public static Specification<ProductDto> withFilterCriteria(ProductSearchCriteria criteria) {
        return Specification
                .where(nameContains(criteria.getName()))
                .and(brandContains(criteria.getBrand()))
                .and(priceGreaterThanOrEqual(criteria.getMinPrice()))
                .and(priceLessThanOrEqual(criteria.getMaxPrice()));
    }

    //Lọc sản phẩm theo các tiêu chí khác nhau (user)
    public static Specification<ProductDto> withFilterCriteriaHome(ProductSearchCriteria criteria) {
        return Specification
                .where(fieldContainsSearchTerm(criteria.getSearchTerm()))
                .and(categoryNameEquals(criteria.getCategoryName()))
                .and(priceGreaterThanOrEqual(criteria.getMinPrice()))
                .and(priceLessThanOrEqual(criteria.getMaxPrice()));
    }

    private static Specification<ProductDto> fieldContainsSearchTerm(String searchTerm) {
        return (root, query, criteriaBuilder) -> {
            if (searchTerm == null || searchTerm.isEmpty()) {
                return null;
            }

            String likePattern = "%" + searchTerm.toLowerCase() + "%";

            return criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("name")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("brand")), likePattern),
                    criteriaBuilder.like(root.get("id").as(String.class), likePattern)
            );
        };
    }

    private static Specification<ProductDto> nameContains(String name) {
        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + name.toLowerCase() + "%"
            );
        };
    }

    private static Specification<ProductDto> brandContains(String brand) {
        return (root, query, criteriaBuilder) -> {
            if (brand == null || brand.isEmpty()) {
                return null;
            }
            return criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("brand")),
                    "%" + brand.toLowerCase() + "%"
            );
        };
    }

    private static Specification<ProductDto> categoryNameEquals(String categoryName) {
        return (root, query, criteriaBuilder) -> {
            if (categoryName == null || categoryName.isEmpty()) {
                return null;
            }
            return criteriaBuilder.equal(
                    criteriaBuilder.lower(root.get("category").get("name")),
                    categoryName.toLowerCase()
            );
        };
    }

    private static Specification<ProductDto> priceGreaterThanOrEqual(BigDecimal price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) {
                return null;
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get("sellingPrice"), price);
        };
    }

    private static Specification<ProductDto> priceLessThanOrEqual(BigDecimal price) {
        return (root, query, criteriaBuilder) -> {
            if (price == null) {
                return null;
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get("sellingPrice"), price);
        };
    }
}
