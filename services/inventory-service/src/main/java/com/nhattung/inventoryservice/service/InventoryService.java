package com.nhattung.inventoryservice.service;


import com.nhattung.inventoryservice.entity.Inventory;
import com.nhattung.inventoryservice.exception.AppException;
import com.nhattung.inventoryservice.exception.ErrorCode;
import com.nhattung.inventoryservice.repository.InventoryRepository;
import com.nhattung.inventoryservice.request.GetQuantityRequest;
import com.nhattung.inventoryservice.request.InventoryRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryService implements IInventoryService {

    private final InventoryRepository inventoryRepository;
    private final RedisTemplate<String, Integer> redisTemplate;
    @Override
    public void updateInventory(InventoryRequest request) {
        inventoryRepository.findByProductId(request.getProductId()).map(inventory -> {
            inventory.setQuantity(request.getQuantity());
            return inventory;
        }).map(inventoryRepository::save).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

    }

    @Override
    public void addInventory(InventoryRequest request) {
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        inventoryRepository.save(Inventory.builder().productId(request.getProductId()).quantity(request.getQuantity()).build());
    }

    @Override
    public void deleteInventory(Long productId) {
        inventoryRepository.findByProductId(productId).ifPresent(inventoryRepository::delete);
    }

    @Override
    public int getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId).map(Inventory::getQuantity).orElse(0);
    }

    @Override
    public boolean checkInventory(GetQuantityRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId()).orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return inventory.getQuantity() >= request.getQuantity();
    }

    @Override
    public Map<String, Boolean> checkInventories(Map<Long, Integer> productQuantities) {
        // 1. Lấy danh sách productId
        Set<Long> productIds = productQuantities.keySet();

        //2. Lấy tồn kho thực tế từ DB
        Map<Long, Integer> availableQuantities = getAvailableQuantitiesFromDB(productIds);

        // 3. Lấy tổng số lượng đang được reserve từ Redis
        Map<Long, Integer> totalReserved = getTotalReservedFromRedis(productIds);

        // 4. So sánh và trả về kết quả
        Map<String, Boolean> result = new HashMap<>();

        for (Map.Entry<Long, Integer> entry : productQuantities.entrySet()) {
            Long productId = entry.getKey();
            Integer requiredQuantity = entry.getValue();
            Integer available = availableQuantities.getOrDefault(productId, 0);
            Integer reserved = totalReserved.getOrDefault(productId, 0);

            int actualAvailable = available - reserved;
            result.put(productId.toString(), actualAvailable >= requiredQuantity);
        }
        return result;
    }

    private Map<Long, Integer> getAvailableQuantitiesFromDB(Set<Long> productIds) {
        List<Inventory> inventories = inventoryRepository.findAllByProductIdIn(productIds);
        return inventories.stream()
                .collect(
                        Collectors.toMap(
                                Inventory::getProductId,
                                Inventory::getQuantity
                        ));
    }

    private Map<Long, Integer> getTotalReservedFromRedis(Set<Long> productIds) {
        Map<Long, Integer> totalReserved = new HashMap<>();

        for (Long productId : productIds) {
            String pattern = "reserve:product:" + productId + ":user:*";
            Set<String> keys = redisTemplate.keys(pattern);

            int reserved = 0;
            if (keys != null) {
                for (String key : keys) {
                    Integer value = redisTemplate.opsForValue().get(key);
                    reserved += (value != null) ? value : 0;
                }
            }

            totalReserved.put(productId, reserved);
        }

        return totalReserved;
    }

    @Override
    public void reserveProduct(String userId, Long productId, Integer quantity) {
        String key = buildReserveKey(userId, productId);

        // Set số lượng giữ hàng với thời gian sống 20 phút
        redisTemplate.opsForValue().set(key, quantity, Duration.ofMinutes(20));
    }

    public record InventoryCompensation(Long productId, int quantityBefore, int quantityDeducted) {}
    @Override
    public Optional<InventoryCompensation> deductInventoryAfterPayment(String userId, Long productId, Integer quantity) {
        String key = buildReserveKey(userId, productId);

        try {
            Inventory inventory = inventoryRepository.findByProductId(productId)
                    .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

            if (inventory.getQuantity() < quantity) {
                return Optional.empty();
            }

            int quantityBefore = inventory.getQuantity(); // Số lượng trước khi trừ
            inventory.setQuantity(quantityBefore - quantity);
            inventoryRepository.save(inventory);
            redisTemplate.delete(key);

            return Optional.of(new InventoryCompensation(productId, quantityBefore, quantity));

        } catch (Exception e) {
            log.error("Error deducting inventory for productId {}: {}", productId, e.getMessage());
            return Optional.empty();
        }

    }
    @Override
    public void rollbackInventory(InventoryCompensation comp) {
        Inventory inventory = inventoryRepository.findByProductId(comp.productId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

        inventory.setQuantity(inventory.getQuantity() + comp.quantityDeducted());
        inventoryRepository.save(inventory);

        // Optional: nếu cần phục hồi key redis đã xóa
        String key = buildReserveKey("rollback", comp.productId());
        redisTemplate.opsForValue().set(key, comp.quantityDeducted(), Duration.ofMinutes(20));
    }
    private String buildReserveKey(String userId, Long productId) {
        return "reserve:product:" + productId + ":user:" + userId;
    }

}
