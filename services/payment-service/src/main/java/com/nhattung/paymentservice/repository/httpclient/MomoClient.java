package com.nhattung.paymentservice.repository.httpclient;

import com.nhattung.paymentservice.request.MoMoPaymentRequest;
import com.nhattung.paymentservice.request.MoMoRefundRequest;
import com.nhattung.paymentservice.response.MoMoPaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "momo", url = "${momo.end-point}")
public interface MomoClient {

    @PostMapping(value = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    MoMoPaymentResponse createPayment(MoMoPaymentRequest momoRequest);

    @PostMapping(value = "/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    MoMoPaymentResponse refundPayment(MoMoRefundRequest momoRequest);
}
