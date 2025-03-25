package com.nhattung.productservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {
    private String name;
    private String description;
    private String brand;
    private BigDecimal costPrice;
    private BigDecimal sellingPrice;
    private String categoryName;
    //private List<ImageRequest> images;
}
