package com.nhattung.deliveryservice.service;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.enums.DeliveryStatus;
import com.nhattung.deliveryservice.exception.AppException;
import com.nhattung.deliveryservice.exception.ErrorCode;
import com.nhattung.deliveryservice.repository.DeliveryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService{

    private final DeliveryRepository deliveryRepository;

    @Override
    public Delivery createDelivery(Delivery delivery) {
        return deliveryRepository.save(delivery);
    }

    @Override
    public Delivery getDeliveryById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public Delivery getDeliveryByStatus(String status) {
        return deliveryRepository.findByShippingStatus(status)
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public Delivery updateDeliveryStatus(Long id, String status) {
        return deliveryRepository.findById(id)
                .map(delivery -> {
                    delivery.setShippingStatus(DeliveryStatus.valueOf(status));
                    return deliveryRepository.save(delivery);
                })
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
}
