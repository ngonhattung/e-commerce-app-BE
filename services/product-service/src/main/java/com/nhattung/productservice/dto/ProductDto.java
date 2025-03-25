package com.nhattung.productservice.dto;

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
    private String name;
    private String description;
    private String brand;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private Long categoryId;
    private List<ImageDto> images;
}
