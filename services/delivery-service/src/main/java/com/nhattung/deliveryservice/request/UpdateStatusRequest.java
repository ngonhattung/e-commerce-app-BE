package com.nhattung.deliveryservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateStatusRequest {

    private String status;
    private Long deliveryId;
    private String orderId;
}
