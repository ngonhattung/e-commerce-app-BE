package com.nhattung.orderservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDto {
    private Long id;
    private String name;
    private String description;
    private String brand;
    private int quantity;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private CategoryDto category;
    private List<ImageDto> images;
}
