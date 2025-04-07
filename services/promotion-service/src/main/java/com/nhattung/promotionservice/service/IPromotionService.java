package com.nhattung.promotionservice.service;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;

import java.util.List;

public interface IPromotionService {

    Promotion savePromotion(CreatePromotionRequest request);
    Promotion getPromotion(Long promotionId);
    Promotion getPromotionByCouponCode(String couponCode);
    Promotion updatePromotion(Long promotionId, UpdatePromotionRequest request);
    List<Promotion> getAllPromotions();
    void deletePromotion(Long promotionId);
    PromotionDto convertToDto(Promotion promotion);
    List<PromotionDto> convertToDto(List<Promotion> promotions);
}
