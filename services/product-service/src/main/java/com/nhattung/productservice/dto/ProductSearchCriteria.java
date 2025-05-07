package com.nhattung.productservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductSearchCriteria {
    private String searchTerm;
    private String name;
    private String brand;
    private String categoryName;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
}
