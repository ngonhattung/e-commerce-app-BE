package com.nhattung.inventoryservice.controller.saga;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySaga {

    private final IInventoryService inventoryService;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    @KafkaListener(topics = "order-created-topic")
    public void handleInventoryProcessing(OrderSagaEvent orderSagaEvent) {

        log.info("Nhận phản hồi từ Order Service: {}", orderSagaEvent);

        var orderItems = orderSagaEvent.getOrder().getOrderItems();
        Map<Long, Integer> productQuantities = new HashMap<>();

        // Gom nhóm số lượng theo productId (nếu có trùng lặp)
        for (var item : orderItems) {
            inventoryService.reserveProduct(orderSagaEvent.getOrder().getUserId(),item.getProductId(), item.getQuantity());
            productQuantities.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }
        // Gọi inventory service 1 lần duy nhất cho tất cả sản phẩm
        Map<String, Boolean> availabilityMap = inventoryService.checkInventories(productQuantities);

        // Kiểm tra toàn bộ sản phẩm có đủ hàng không
        boolean isAllAvailable = availabilityMap.values().stream().allMatch(Boolean::booleanValue);

        if(isAllAvailable){
            for (var item : orderItems) {
                inventoryService.reserveProduct(
                        orderSagaEvent.getOrder().getUserId(),
                        item.getProductId(),
                        item.getQuantity()
                );
            }
            orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_CHECKED);
            orderSagaEvent.setMessage("Checking inventory completed, all products are available");
        }else {
            orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_FAILED);
            orderSagaEvent.setMessage("Checking inventory completed, some products are not available");
        }
        kafkaTemplate.send("inventory-checkingResponse-topic", orderSagaEvent);
    }



}
