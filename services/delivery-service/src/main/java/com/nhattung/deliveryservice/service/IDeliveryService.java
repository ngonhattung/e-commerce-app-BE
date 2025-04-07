package com.nhattung.deliveryservice.service;

import com.nhattung.deliveryservice.entity.Delivery;

import java.util.List;

public interface IDeliveryService {

    Delivery createDelivery(Delivery delivery);
    Delivery getDeliveryById(Long id);
    Delivery getDeliveryByStatus(String status);
    Delivery updateDeliveryStatus(Long id, String status);
    List<Delivery> getAllDeliveries();

}
