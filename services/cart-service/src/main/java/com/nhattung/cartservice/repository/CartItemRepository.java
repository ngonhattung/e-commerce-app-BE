package com.nhattung.cartservice.repository;

import com.nhattung.cartservice.entity.Cart;
import com.nhattung.cartservice.entity.CartItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    void deleteByCartId(Long id);

    Page<CartItem> findAllByCartId(Long CartId, Pageable pageable);
}
