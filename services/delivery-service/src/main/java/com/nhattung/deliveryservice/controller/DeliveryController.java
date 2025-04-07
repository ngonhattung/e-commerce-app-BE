package com.nhattung.deliveryservice.controller;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.response.ApiResponse;
import com.nhattung.deliveryservice.service.IDeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/delivery")
public class DeliveryController {

    private final IDeliveryService deliveryService;

    @GetMapping("/get-by-id/{deliveryId}")
    public ApiResponse<Delivery> getDeliveryById(@PathVariable Long deliveryId) {
        Delivery delivery = deliveryService.getDeliveryById(deliveryId);
        return ApiResponse.<Delivery>builder()
                .message("Get delivery by id successfully")
                .result(delivery)
                .build();
    }
    @GetMapping("/get-by-status/{status}")
    public ApiResponse<Delivery> getDeliveryByStatus(@PathVariable String status) {
        Delivery delivery = deliveryService.getDeliveryByStatus(status);
        return ApiResponse.<Delivery>builder()
                .message("Get delivery by status successfully")
                .result(delivery)
                .build();
    }

    @GetMapping("/get-all")
    public ApiResponse<List<Delivery>> getAllDeliveries() {
        List<Delivery> deliveries = deliveryService.getAllDeliveries();
        return ApiResponse.<List<Delivery>>builder()
                .message("Get all deliveries successfully")
                .result(deliveries)
                .build();
    }

    @PutMapping("/update-status/{deliveryId}/{status}")
    public ApiResponse<Delivery> updateDeliveryStatus(@PathVariable Long deliveryId,
                                                      @PathVariable String status) {
        Delivery delivery = deliveryService.updateDeliveryStatus(deliveryId, status);
        return ApiResponse.<Delivery>builder()
                .message("Update delivery status successfully")
                .result(delivery)
                .build();
    }
}
