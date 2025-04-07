package com.nhattung.orderservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SelectedCartItemRequest {
    private List<Long> selectedCartItemIds;
    @Builder.Default
    private String couponCode = "DEFAULT";
}
