package com.nhattung.paymentservice.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MoMoCallbackRequest {
    private String partnerCode;
    private String accessKey;
    private String requestId;
    private Long amount;
    private String orderId;
    private String orderInfo;
    private String orderType;
    private Long transId;
    private int resultCode;
    private String message;
    private String payType;
    private Long responseTime;
    private String extraData;
    private String signature;
}
