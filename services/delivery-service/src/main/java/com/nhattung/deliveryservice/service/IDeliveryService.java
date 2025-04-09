package com.nhattung.deliveryservice.service;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.request.UpdateStatusRequest;

import java.util.List;

public interface IDeliveryService {

    void createDelivery(Delivery delivery);
    Delivery getDeliveryById(Long id);
    Delivery getDeliveryByStatus(String status);
    Delivery updateDeliveryStatus(UpdateStatusRequest request);
    List<Delivery> getAllDeliveries();

}
