package com.nhattung.productservice.repository;

import com.nhattung.productservice.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    boolean existsByNameAndBrand(String name, String brand);

    List<Product> findByCategoryName(String category);
    Page<Product> findByCategoryName(String category, Pageable pageable);

    List<Product> findByBrand(String brand);
    Page<Product> findByBrand(String brand, Pageable pageable);

    List<Product> findByCategoryNameAndBrand(String category, String brand);
    Page<Product> findByCategoryNameAndBrand(String category, String brand, Pageable pageable);

    List<Product> findByName(String name);
    Page<Product> findByName(String name, Pageable pageable);

    List<Product> findByBrandAndName(String brand, String name);
    Page<Product> findByBrandAndName(String brand, String name, Pageable pageable);
    Long countByBrandAndName(String brand, String name);
}
