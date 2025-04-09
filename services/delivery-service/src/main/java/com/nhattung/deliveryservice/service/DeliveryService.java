package com.nhattung.deliveryservice.service;

import com.nhattung.deliveryservice.entity.Delivery;
import com.nhattung.deliveryservice.exception.AppException;
import com.nhattung.deliveryservice.exception.ErrorCode;
import com.nhattung.deliveryservice.repository.DeliveryRepository;
import com.nhattung.deliveryservice.request.UpdateStatusRequest;
import com.nhattung.enums.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryService implements IDeliveryService{

    private final DeliveryRepository deliveryRepository;

    @Override
    public void createDelivery(Delivery delivery) {
        deliveryRepository.save(delivery);
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
    public Delivery updateDeliveryStatus(UpdateStatusRequest request) {
        return deliveryRepository.findById(request.getDeliveryId())
                .map(delivery -> {
                    delivery.setShippingStatus(OrderStatus.valueOf(request.getStatus()));
                    return deliveryRepository.save(delivery);
                })
                .orElseThrow(() -> new AppException(ErrorCode.DELIVERY_NOT_FOUND));
    }

    @Override
    public List<Delivery> getAllDeliveries() {
        return deliveryRepository.findAll();
    }
}
