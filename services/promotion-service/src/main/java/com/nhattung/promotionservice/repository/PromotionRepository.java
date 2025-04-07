package com.nhattung.promotionservice.repository;

import com.fasterxml.jackson.databind.introspect.AnnotationCollector;
import com.nhattung.promotionservice.entity.Promotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PromotionRepository extends JpaRepository<Promotion, Long> {


    boolean existsPromotionByCouponCode(String couponCode);

    Optional<Promotion> findByCouponCode(String couponCode);
}
