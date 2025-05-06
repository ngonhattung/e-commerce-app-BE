package com.nhattung.orderservice.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductSalesDto {

    private Long productId;
    private BigDecimal revenue;
    private BigDecimal price;
    private Long sold;
}
