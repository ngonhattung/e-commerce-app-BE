package com.nhattung.inventoryservice.service;


import com.nhattung.inventoryservice.entity.Inventory;
import com.nhattung.inventoryservice.exception.AppException;
import com.nhattung.inventoryservice.exception.ErrorCode;
import com.nhattung.inventoryservice.repository.InventoryRepository;
import com.nhattung.inventoryservice.request.GetQuantityRequest;
import com.nhattung.inventoryservice.request.InventoryRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryService implements IInventoryService {

    private final InventoryRepository inventoryRepository;

    @Override
    public void updateInventory(InventoryRequest request) {
        inventoryRepository.findByProductId(request.getProductId())
                .map(inventory -> {
                    inventory.setQuantity(request.getQuantity());
                    return inventory;
                })
                .map(inventoryRepository::save)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));

    }

    @Override
    public void addInventory(InventoryRequest request) {
        if (request.getQuantity() <= 0) {
            throw new AppException(ErrorCode.INVALID_QUANTITY);
        }
        inventoryRepository.save(Inventory.builder()
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .build());
    }

    @Override
    public void deleteInventory(Long productId) {
        inventoryRepository.findByProductId(productId)
                .ifPresent(inventoryRepository::delete);
    }

    @Override
    public int getInventory(Long productId) {
        return inventoryRepository.findByProductId(productId)
                .map(Inventory::getQuantity)
                .orElse(0);
    }

    @Override
    public boolean checkInventory(GetQuantityRequest request) {
        Inventory inventory = inventoryRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_NOT_FOUND));
        return inventory.getQuantity() >= request.getQuantity();
    }
}
