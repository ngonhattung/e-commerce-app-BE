package com.nhattung.cartservice.dto;

import com.nhattung.cartservice.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartDto {
    private Long id;
    private BigDecimal totalAmount;
    private String userId;
    private Set<CartItemDto> items;
}
