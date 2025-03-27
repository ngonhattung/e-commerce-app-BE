package com.nhattung.inventory_service.controller;

import com.nhattung.inventory_service.request.InventoryRequest;
import com.nhattung.inventory_service.response.ApiResponse;
import com.nhattung.inventory_service.service.IInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventory")
public class InventoryController {

    private final IInventoryService inventoryService;

    @PostMapping("/add")
    public ApiResponse<Void> addInventory(@RequestBody InventoryRequest request) {
        inventoryService.addInventory(request);
        return ApiResponse.<Void>builder()
                .message("Inventory added successfully")
                .build();
    }

    @PostMapping("/update")
    public ApiResponse<Void> updateInventory(@RequestBody InventoryRequest request) {
        inventoryService.updateInventory(request);
        return ApiResponse.<Void>builder()
                .message("Inventory updated successfully")
                .build();
    }

    @PostMapping("/delete/{productId}")
    public ApiResponse<Void> deleteInventory(@PathVariable("productId") Long productId) {
        inventoryService.deleteInventory(productId);
        return ApiResponse.<Void>builder()
                .message("Inventory deleted successfully")
                .build();
    }

    @GetMapping("/{productId}")
    public ApiResponse<Integer> getInventory(@PathVariable("productId") Long productId) {
        return ApiResponse.<Integer>builder()
                .result(inventoryService.getInventory(productId))
                .build();
    }

}
