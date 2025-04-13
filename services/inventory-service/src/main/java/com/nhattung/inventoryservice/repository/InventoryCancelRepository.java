package com.nhattung.inventoryservice.repository;

import com.nhattung.inventoryservice.entity.InventoryCancel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryCancelRepository extends JpaRepository<InventoryCancel, Long> {
    Optional<InventoryCancel> findByProductId(Long productId);
}
