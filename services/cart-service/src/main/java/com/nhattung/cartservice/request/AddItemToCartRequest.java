package com.nhattung.cartservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddItemToCartRequest {
    private Long userId;
    private Long productId;
    private int quantity;

}
