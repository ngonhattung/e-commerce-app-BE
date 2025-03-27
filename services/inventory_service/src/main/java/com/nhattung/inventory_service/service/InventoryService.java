package com.nhattung.inventory_service.service;


import com.nhattung.inventory_service.entity.Inventory;
import com.nhattung.inventory_service.exception.AppException;
import com.nhattung.inventory_service.exception.ErrorCode;
import com.nhattung.inventory_service.repository.InventoryRepository;
import com.nhattung.inventory_service.request.InventoryRequest;
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
}
