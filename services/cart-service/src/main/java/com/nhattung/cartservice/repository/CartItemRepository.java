package com.nhattung.cartservice.repository;

import com.nhattung.cartservice.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    // Custom query methods can be defined here if needed
}
