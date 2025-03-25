package com.nhattung.productservice.request;

import com.nhattung.productservice.entity.Category;
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
public class CreateProductRequest {
    private String name;
    private String description;
    private String brand;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String categoryName;
    //private List<ImageDto> images;
}
