package com.nhattung.inventory_service.service;

import com.nhattung.inventory_service.request.InventoryRequest;

public interface IInventoryService {

     void updateInventory(InventoryRequest request);

     void addInventory(InventoryRequest request);

     void deleteInventory(Long productId);

     int getInventory(Long productId);
}
