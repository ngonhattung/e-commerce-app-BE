package com.nhattung.paymentservice.controller.saga;

import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.paymentservice.entity.Payment;
import com.nhattung.paymentservice.enums.PaymentMethod;
import com.nhattung.paymentservice.service.momo.MomoService;
import com.nhattung.paymentservice.service.payment.IPaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentSaga {
    private final IPaymentService paymentService;

    @KafkaListener(topics = "payment-processing-topic")
    public void processPayment(OrderSagaEvent orderSagaEvent) {
        log.info("Received order created event: {}", orderSagaEvent);

        Payment payment = Payment.builder()
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.MOMO)
                .totalAmount(orderSagaEvent.getOrder().getTotalPrice())
                .paymentStatus(OrderStatus.PAYMENT_PROCESSING)
                .orderId(orderSagaEvent.getOrder().getOrderId())
                .build();
        paymentService.savePayment(payment);
    }
}
