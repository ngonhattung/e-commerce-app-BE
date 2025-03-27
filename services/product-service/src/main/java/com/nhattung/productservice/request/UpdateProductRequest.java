package com.nhattung.productservice.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotBlank(message = "EMPTY_PRODUCT_NAME")
    private String name;

    private String description;

    @Min(value = 0, message = "INVALID_QUANTITY")
    private int quantity;

    @NotBlank(message = "EMPTY_BRAND")
    private String brand;


    @NotNull(message = "EMPTY_COST_PRICE")
    @DecimalMin(value = "0.0",inclusive = false, message = "INVALID_COST_PRICE")
    private BigDecimal costPrice;

    @NotNull(message = "EMPTY_SELLING_PRICE")
    @DecimalMin(value = "0.0",inclusive = false, message = "INVALID_SELLING_PRICE")
    private BigDecimal sellingPrice;

    @NotBlank(message = "EMPTY_CATEGORY_NAME")
    private String categoryName;
}
