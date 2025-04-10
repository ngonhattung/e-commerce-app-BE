package com.nhattung.deliveryservice.controller;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.request.UpdateStatusRequest;
import com.nhattung.deliveryservice.response.ApiResponse;
import com.nhattung.deliveryservice.service.IDeliveryService;
import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class DeliveryController {

    private final IDeliveryService deliveryService;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    @GetMapping("/get-by-id/{deliveryId}")
    public ApiResponse<Delivery> getDeliveryById(@PathVariable Long deliveryId) {
        Delivery delivery = deliveryService.getDeliveryById(deliveryId);
        return ApiResponse.<Delivery>builder()
                .message("Get delivery by id successfully")
                .result(delivery)
                .build();
    }
    @GetMapping("/get-by-status/{status}")
    public ApiResponse<Delivery> getDeliveryByStatus(@PathVariable String status) {
        Delivery delivery = deliveryService.getDeliveryByStatus(status);
        return ApiResponse.<Delivery>builder()
                .message("Get delivery by status successfully")
                .result(delivery)
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<Delivery>> getAllDeliveries() {
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        return ApiResponse.<List<Delivery>>builder()
                .message("Get all deliveries successfully")
                .result(deliveries)
                .build();
    }

    @PutMapping("/update-status")
    public ApiResponse<Delivery> updateDeliveryStatus(@RequestBody UpdateStatusRequest request) {
        if(request.getStatus() == null || request.getStatus().isEmpty()) {
            return ApiResponse.<Delivery>builder()
                    .message("Status cannot be null or empty")
                    .build();
        }
        Delivery delivery = deliveryService.updateDeliveryStatus(request);
        OrderSagaEvent orderSagaEvent = getOrderSagaEvent(request);
        kafkaTemplate.send("delivery-response-topic", orderSagaEvent);

        return ApiResponse.<Delivery>builder()
                .message("Update delivery status successfully")
                .result(delivery)
                .build();
    }

    private OrderSagaEvent getOrderSagaEvent(UpdateStatusRequest request) {
        OrderSagaEvent orderSagaEvent = new OrderSagaEvent();
        orderSagaEvent.setOrder(new OrderSagaDto(request.getOrderId()));
        if (OrderStatus.DELIVERY_COMPLETED.name().equals(request.getStatus())) {
            orderSagaEvent.setOrderStatus(OrderStatus.DELIVERY_COMPLETED);
            orderSagaEvent.setMessage("Delivery completed");
        } else if (OrderStatus.DELIVERY_FAILED.name().equals(request.getStatus())) {
            orderSagaEvent.setOrderStatus(OrderStatus.DELIVERY_FAILED);
            orderSagaEvent.setMessage("Delivery failed");

        }
        return orderSagaEvent;
    }
}
