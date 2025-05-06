package com.nhattung.promotionservice.repository;

import com.nhattung.promotionservice.entity.UserPromotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPromotionRepository extends JpaRepository<UserPromotion, Long> {
    Optional<UserPromotion> findByUserIdAndPromotionId(String userId, Long promotionId);


}
