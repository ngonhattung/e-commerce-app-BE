package com.nhattung.inventoryservice.controller.saga;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.inventoryservice.service.IInventoryService;
import com.nhattung.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventorySaga {

    private final IInventoryService inventoryService;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    private final RedisTemplate<String, Object> redisTemplate;
    private final Map<String, List<InventoryService.InventoryCompensation>> compensationMap = new ConcurrentHashMap<>();
    @KafkaListener(topics = "order-created-topic") // khi tạo đơn hàng thành công
    public void handleInventoryChecking(OrderSagaEvent orderSagaEvent) {

        log.info("Nhận phản hồi từ Order Service: {}", orderSagaEvent);

        var orderItems = orderSagaEvent.getOrder().getOrderItems();
        Map<Long, Integer> productQuantities = new HashMap<>();

        // Gom nhóm số lượng theo productId (nếu có trùng lặp)
        for (var item : orderItems) {
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
        redisTemplate.opsForValue().set(orderSagaEvent.getOrder().getOrderId(), orderSagaEvent, Duration.ofMinutes(5));
    }

    @KafkaListener(topics = "inventory-processing-topic")  // khi thanh toán thành công
    public void handleInventoryProcessing(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);
        if (orderSagaEvent.getOrderStatus() == OrderStatus.INVENTORY_PROCESSING) {
            // Xử lý subtract quantity
            String orderId = orderSagaEvent.getOrder().getOrderId();
            var orderItems = orderSagaEvent.getOrder().getOrderItems();
            List<InventoryService.InventoryCompensation> compensations = new ArrayList<>();
            boolean allSuccess = true;

            for (var item : orderItems) {
                Optional<InventoryService.InventoryCompensation> result = inventoryService.deductInventoryAfterPayment(
                        orderSagaEvent.getOrder().getUserId(),
                        item.getProductId(),
                        item.getQuantity()
                );

                if (result.isPresent()) {
                    compensations.add(result.get());
                } else {
                    allSuccess = false;
                    break;
                }
            }

            if (allSuccess) {
                // Lưu danh sách thao tác để dùng khi cần rollback (trường hợp cancel)
                compensationMap.put(orderId, compensations);
                orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_COMPLETED);
                orderSagaEvent.setMessage("Inventory processing completed");
            } else {
                // ROLLBACK đã trừ
                for (InventoryService.InventoryCompensation comp : compensations) {
                    inventoryService.rollbackInventory(comp);
                }
                orderSagaEvent.setOrderStatus(OrderStatus.INVENTORY_FAILED);
                orderSagaEvent.setMessage("Inventory processing failed during deduction");
            }
            kafkaTemplate.send("inventory-response-topic", orderSagaEvent);

        }
    }

    @KafkaListener(topics = "inventory-revert-topic") //khi giao hàng thất bại
    public void handleInventoryRevert(OrderSagaEvent orderSagaEvent) {
        log.info("Nhận phản hồi từ Inventory Service: {}", orderSagaEvent);
        if (orderSagaEvent.getOrderStatus() == OrderStatus.DELIVERY_FAILED) {
            // Xử lý revert quantity
            String orderId = orderSagaEvent.getOrder().getOrderId();
            List<InventoryService.InventoryCompensation> comps = compensationMap.get(orderId);
            if (comps != null) {
                comps.forEach(inventoryService::rollbackInventory);
                compensationMap.remove(orderId); // Dọn dữ liệu
            }
        }
    }
}
