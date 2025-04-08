package com.nhattung.orderservice.controller.saga;

import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.orderservice.dto.OrderDto;
import com.nhattung.orderservice.entity.Order;
import com.nhattung.orderservice.exception.AppException;
import com.nhattung.orderservice.exception.ErrorCode;
import com.nhattung.orderservice.repository.OrderRepository;
import com.nhattung.orderservice.service.IOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderSaga {

    private final OrderRepository orderRepository;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    @KafkaListener(topics = "payment-response-topic")
    @Transactional
    public void handlePaymentResponse(OrderSagaEvent orderSagaEvent)
    {
        log.info("Nhận phản hồi từ Payment Service: {}", orderSagaEvent);

        Order order = orderRepository.findById(orderSagaEvent.getOrder().getOrderId())
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if(orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_COMPLETED)
        {
            order.setOrderStatus(OrderStatus.PAYMENT_COMPLETED);
            orderRepository.save(order);

            orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_PROCESSING);
            orderSagaEvent.setMessage("Payment completed, processing inventory");
            kafkaTemplate.send("inventory-processing-topic", orderSagaEvent);
        } else if (orderSagaEvent.getOrderStatus() == OrderStatus.PAYMENT_FAILED) {

            order.setOrderStatus(OrderStatus.ORDER_CANCELLED);
            orderRepository.save(order);

            orderSagaEvent.setMessage("Payment failed, cancelling order");
            kafkaTemplate.send("order-cancellation-topic", orderSagaEvent);
        }


    }
}
