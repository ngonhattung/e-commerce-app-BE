package com.nhattung.productservice.repository.httpclient;

import com.nhattung.productservice.request.InventoryRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "inventory-service")
public interface InventoryClient {

    @PostMapping(value = "/inventory/add",produces = MediaType.APPLICATION_JSON_VALUE)
    void addInventory(@RequestBody InventoryRequest request);

    @PostMapping(value = "/inventory/update",produces = MediaType.APPLICATION_JSON_VALUE)
    void updateInventory(@RequestBody InventoryRequest request);

    @PostMapping(value = "/inventory/delete",produces = MediaType.APPLICATION_JSON_VALUE)
    void deleteInventory(@RequestBody Long productId);

}
