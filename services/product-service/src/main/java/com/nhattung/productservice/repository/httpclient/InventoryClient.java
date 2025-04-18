package com.nhattung.productservice.repository.httpclient;

import com.nhattung.productservice.request.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping(value = "/inventory/add",produces = MediaType.APPLICATION_JSON_VALUE)
    void addInventory(@RequestBody InventoryRequest request);

    @PutMapping(value = "/inventory/update",produces = MediaType.APPLICATION_JSON_VALUE)
    void updateInventory(@RequestBody InventoryRequest request);

    @DeleteMapping(value = "/inventory/delete/{productId}",produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteInventory(@PathVariable("productId") Long productId);

    @GetMapping(value = "/inventory/gets",produces = MediaType.APPLICATION_JSON_VALUE)
    Map<Long, Integer> getInventory(@RequestParam("productIds") Set<Long> productIds);

}
