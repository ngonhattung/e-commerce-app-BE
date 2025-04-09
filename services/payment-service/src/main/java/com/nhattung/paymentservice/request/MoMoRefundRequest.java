package com.nhattung.paymentservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoMoRefundRequest {
    private String partnerCode;
    private String orderId;
    private String requestId;
    private Long amount;
    private String accessKey;
    private String lang;
    private String description;
    private String signature;
}
