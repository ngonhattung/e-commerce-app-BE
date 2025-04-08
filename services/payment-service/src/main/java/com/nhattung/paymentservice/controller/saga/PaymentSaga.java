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

    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    private final MomoService momoService;
    private final IPaymentService paymentService;

    @KafkaListener(topics = "order-created-topic")
    public void processPayment(OrderSagaEvent orderSagaEvent) {
        log.info("Received order created event: {}", orderSagaEvent);

        Payment payment = Payment.builder()
                .paymentDate(LocalDate.now())
                .paymentMethod(PaymentMethod.MOMO)
                .totalAmount(orderSagaEvent.getOrder().getTotalPrice())
                .paymentStatus(OrderStatus.PAYMENT_PROCESSING)
                .orderId(orderSagaEvent.getOrder().getOrderId())
                .build();
        OrderSagaEvent paymentSagaEvent = new OrderSagaEvent();
        paymentSagaEvent.setOrder(orderSagaEvent.getOrder());
        if (momoService.isPaymentSuccess) {
            payment.setPaymentStatus(OrderStatus.PAYMENT_COMPLETED);
            paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
            paymentSagaEvent.setMessage("Payment completed successfully");
        } else {
            payment.setPaymentStatus(OrderStatus.PAYMENT_FAILED);
            paymentSagaEvent.setOrderStatus(OrderStatus.PAYMENT_FAILED);
            paymentSagaEvent.setMessage("Payment failed");
        }
        paymentService.savePayment(payment);
        kafkaTemplate.send("payment-response-topic", paymentSagaEvent);
        log.info("Sent payment saga event: {}", paymentSagaEvent);
    }
}
