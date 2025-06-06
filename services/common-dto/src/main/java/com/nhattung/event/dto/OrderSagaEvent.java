package com.nhattung.event.dto;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSagaEvent {
    private OrderSagaDto order;
    private OrderStatus orderStatus;
    private String message;
}
