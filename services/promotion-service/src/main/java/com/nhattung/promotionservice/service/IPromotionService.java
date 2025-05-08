package com.nhattung.promotionservice.service;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.dto.PromotionSearchCriteria;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.HandleUserPromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;
import com.nhattung.promotionservice.response.PageResponse;

import java.util.List;

public interface IPromotionService {

    Promotion savePromotion(CreatePromotionRequest request);
    Promotion getPromotion(Long promotionId);
    Promotion getPromotionByCouponCode(String couponCode);
    Promotion getPromotionActiveByCouponCode(String couponCode);
    Promotion updatePromotion(Long promotionId, UpdatePromotionRequest request);
    PageResponse<PromotionDto> getAllPromotions(int page, int size);
    List<Promotion> getAllActivePromotions();
    void deletePromotion(Long promotionId);
    PromotionDto convertToDto(Promotion promotion);
    List<PromotionDto> convertToDto(List<Promotion> promotions);
    void createUserPromotion(HandleUserPromotionRequest request);
    void updateUserPromotion(HandleUserPromotionRequest request);
    PageResponse<PromotionDto> searchPromotions(PromotionSearchCriteria criteria, int page, int size);
    PageResponse<PromotionDto> filterPromotions(PromotionSearchCriteria criteria, int page, int size);
}
