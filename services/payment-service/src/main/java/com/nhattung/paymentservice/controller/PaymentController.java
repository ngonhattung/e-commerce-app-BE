package com.nhattung.paymentservice.controller;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.paymentservice.request.MoMoCallbackRequest;
import com.nhattung.paymentservice.request.PaymentRequest;
import com.nhattung.paymentservice.response.ApiResponse;
import com.nhattung.paymentservice.response.MoMoPaymentResponse;
import com.nhattung.paymentservice.service.momo.MomoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/payment")
@Slf4j
public class PaymentController {

    private final MomoService momoService;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    @PostMapping("/create-momo-payment")
    public ApiResponse<MoMoPaymentResponse> createMomoPayment(@RequestBody PaymentRequest request) {
        return ApiResponse.<MoMoPaymentResponse>builder()
                .message("Payment created successfully with Order ID: " + request.getOrderId())
                .result(momoService.createPayment(request))
                .build();
    }

    @PostMapping("/refund")
    public ApiResponse<MoMoPaymentResponse> refundPayment(@RequestBody PaymentRequest request) {

//        OrderSagaEvent paymentSagaEvent = new OrderSagaEvent();
//        paymentSagaEvent.setOrder(new OrderSagaDto(request.getOrderId()));

        MoMoPaymentResponse response = momoService.refundPayment(request);
        log.info("Response successfully: {}", response);
//        if(response.getResultCode() == 0)
//        {
//            paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_REFUND_COMPLETED);
//            paymentSagaEvent.setMessage("Refund request sent successfully");
//            kafkaTemplate.send("payment-refundResponse-topic", paymentSagaEvent);
//        } else {
//            paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_REFUND_FAILED);
//            paymentSagaEvent.setMessage("Refund request failed");
//            kafkaTemplate.send("payment-refundResponse-topic", paymentSagaEvent);
//        }

        return ApiResponse.<MoMoPaymentResponse>builder()
                .message("Refund request sent successfully")
                .result(response)
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
