package com.nhattung.deliveryservice.controller.saga;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.enums.DeliveryMethod;
import com.nhattung.deliveryservice.service.IDeliveryService;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class DeliverySaga {

    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    private final IDeliveryService deliveryService;
    @KafkaListener(topics = "delivery-processing-topic")
    public void processDelivery(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Order Service: {}", orderSagaEvent);

        if(OrderStatus.DELIVERY_PROCESSING == orderSagaEvent.getOrderStatus()){
            Delivery delivery = Delivery.builder()
                    .orderId(orderSagaEvent.getOrder().getOrderId())
                    .shippingStatus(orderSagaEvent.getOrderStatus())
                    .shippingMethod(DeliveryMethod.STANDARD)
                    .shippingAddress(orderSagaEvent.getOrder().getShippingAddress())
                    .shippingDate(Instant.now())
                    .build();
            deliveryService.createDelivery(delivery);
        }
    }
}
