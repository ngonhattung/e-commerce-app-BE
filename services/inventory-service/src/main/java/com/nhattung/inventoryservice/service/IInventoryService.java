package com.nhattung.inventoryservice.service;

import com.nhattung.inventoryservice.request.GetQuantityRequest;
import com.nhattung.inventoryservice.request.InventoryRequest;

public interface IInventoryService {

     void updateInventory(InventoryRequest request);

     void addInventory(InventoryRequest request);

     void deleteInventory(Long productId);

     int getInventory(Long productId);

     boolean checkInventory(GetQuantityRequest request);

}
