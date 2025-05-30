package com.nhattung.inventoryservice.controller;

import com.nhattung.dto.OrderSagaDto;
import com.nhattung.enums.OrderStatus;
import com.nhattung.event.dto.OrderSagaEvent;
import com.nhattung.inventoryservice.request.GetQuantityRequest;
import com.nhattung.inventoryservice.request.InventoryRequest;
import com.nhattung.inventoryservice.response.ApiResponse;
import com.nhattung.inventoryservice.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final IInventoryService inventoryService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final KafkaTemplate<String, OrderSagaEvent> kafkaTemplate;
    @PostMapping("/add")
    public ApiResponse<Void> addInventory(@RequestBody InventoryRequest request) {
        inventoryService.addInventory(request);
        return ApiResponse.<Void>builder()
                .message("Inventory added successfully")
                .build();
    }

    @PutMapping("/update")
    public ApiResponse<Void> updateInventory(@RequestBody InventoryRequest request) {
        inventoryService.updateInventory(request);
        return ApiResponse.<Void>builder()
                .message("Inventory updated successfully")
                .build();
    }

    @DeleteMapping("/delete/{productId}")
    public ApiResponse<Void> deleteInventory(@PathVariable("productId") Long productId) {
        inventoryService.deleteInventory(productId);
        return ApiResponse.<Void>builder()
                .message("Inventory deleted successfully")
                .build();
    }

    @GetMapping("/gets")
    public Map<Long, Integer> getInventory(@RequestParam("productIds")Set<Long> productIds) {
        return inventoryService.getInventory(productIds);
    }

    @PostMapping("/checkToCart")
    public ApiResponse<Boolean> checkInventoryToCart(@RequestBody GetQuantityRequest request) {
        return ApiResponse.<Boolean>builder()
                .result(inventoryService.checkInventory(request))
                .build();
    }

    @GetMapping("/checkToOrder/{orderId}")
    public ApiResponse<Boolean> checkInventoryToOrder(@PathVariable("orderId") String orderId) {

        OrderSagaEvent inventoryEvent = (OrderSagaEvent) redisTemplate.opsForValue().get(orderId);
        boolean flag = false;

        if(inventoryEvent == null) {
            return ApiResponse.<Boolean>builder()
                    .result(false)
                    .build();
        }
        if(inventoryEvent.getOrderStatus() == OrderStatus.INVENTORY_CHECKED) {
            flag = true;
        }
        redisTemplate.delete(orderId);
        kafkaTemplate.send("inventory-checkingResponse-topic", inventoryEvent);
        return ApiResponse.<Boolean>builder()
                .result(flag)
                .build();
    }

}
