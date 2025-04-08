package com.nhattung.paymentservice.controller;

import com.nhattung.paymentservice.request.MoMoCallbackRequest;
import com.nhattung.paymentservice.request.PaymentRequest;
import com.nhattung.paymentservice.response.ApiResponse;
import com.nhattung.paymentservice.response.MoMoPaymentResponse;
import com.nhattung.paymentservice.service.momo.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final MomoService momoService;

    @PostMapping("/create-momo-payment")
    public ApiResponse<MoMoPaymentResponse> createMomoPayment(@RequestBody PaymentRequest request) {
        return ApiResponse.<MoMoPaymentResponse>builder()
                .message("Payment created successfully with Order ID: " + request.getOrderId())
                .result(momoService.createPayment(request))
                .build();
    }

    @PostMapping("/refund")
    public ApiResponse<MoMoPaymentResponse> refundPayment(@RequestBody PaymentRequest request) {
        return ApiResponse.<MoMoPaymentResponse>builder()
                .message("Refund request sent successfully")
                .result(momoService.refundPayment(request))
                .build();
    }

    @PostMapping("/ipn-handler")
    public String handleMoMoCallback(@RequestBody MoMoCallbackRequest request) {
        log.info("Received MoMo callback: {}", request);
        momoService.processPaymentResponse(request);
        return "OK";
    }

    @GetMapping("/notify")
    public String notifyPayment() {
        log.info("Payment notification received");
        return "Payment notification received";
    }
}
