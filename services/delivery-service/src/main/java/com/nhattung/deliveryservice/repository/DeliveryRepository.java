package com.nhattung.deliveryservice.repository;

import com.nhattung.deliveryservice.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByShippingStatus(String status);
}
