package com.nhattung.inventoryservice.service;

import com.nhattung.inventoryservice.request.GetQuantityRequest;
import com.nhattung.inventoryservice.request.InventoryRequest;

import java.util.Map;
import java.util.Optional;

public interface IInventoryService {

     void updateInventory(InventoryRequest request);

     void addInventory(InventoryRequest request);

     void deleteInventory(Long productId);

     int getInventory(Long productId);

     boolean checkInventory(GetQuantityRequest request);

     Map<String, Boolean> checkInventories(Map<Long, Integer> productQuantities);
     void reserveProduct(String userId, Long productId, Integer quantity);
     Optional<InventoryService.InventoryCompensation> deductInventoryAfterPayment(String userId, Long productId, Integer quantity);
     void rollbackInventory(InventoryService.InventoryCompensation comp);
}
