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
public class MoMoPaymentRequest {
    private String partnerCode;
    private String requestType;
    private String ipnUrl;
    private Long orderId;
    private BigDecimal amount;
    private String orderInfo;
    private String requestId;
    private String redirectUrl;
    private String lang;
    private String extraData;
    private String signature;
}
