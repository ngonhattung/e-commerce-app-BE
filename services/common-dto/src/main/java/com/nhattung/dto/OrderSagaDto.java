package com.nhattung.dto;

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
public class OrderSagaDto {
    private Long orderId;
    private String userId;
    private BigDecimal totalPrice;
    private String shippingAddress;
    private Set<OrderItemSagaDto> orderItems;

    public OrderSagaDto(Long orderId) {
        this.orderId = orderId;
    }
}
