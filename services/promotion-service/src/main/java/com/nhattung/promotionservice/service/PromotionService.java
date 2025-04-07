package com.nhattung.promotionservice.service;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.exception.AppException;
import com.nhattung.promotionservice.exception.ErrorCode;
import com.nhattung.promotionservice.repository.PromotionRepository;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService{

    private final PromotionRepository promotionRepository;
    private final ModelMapper modelMapper;

    @Override
    public Promotion savePromotion(CreatePromotionRequest request) {
        if(isPromotionExists(request.getCouponCode())) {
            throw new AppException(ErrorCode.PROMOTION_ALREADY_EXISTS);
        }
        Promotion promotion = createPromotion(request);
        return promotionRepository.save(promotion);
    }

    private boolean isPromotionExists(String couponCode) {
        return promotionRepository.existsPromotionByCouponCode(couponCode);
    }

    private Promotion createPromotion(CreatePromotionRequest request) {
        return Promotion.builder()
                .promotionName(request.getPromotionName())
                .description(request.getDescription())
                .couponCode(request.getCouponCode())
                .discountPercent(request.getDiscountPercent())
                .discountAmount(request.getDiscountAmount())
                .minimumOrderValue(request.getMinimumOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(request.getIsActive())
                .build();
    }
    @Override
    public Promotion getPromotion(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }

    @Override
    public Promotion getPromotionByCouponCode(String couponCode) {
        return promotionRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }

    @Override
    public Promotion getPromotionActiveByCouponCode(String couponCode) {
        Promotion promotion = promotionRepository.findByCouponCode(couponCode)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        if (!promotion.getIsActive()) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        }
        if(promotion.getStartDate() != null && promotion.getStartDate().isAfter(Instant.now())) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        }
        if(promotion.getEndDate() != null && promotion.getEndDate().isBefore(Instant.now())) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        }
        return promotion;
    }

    @Override
    public Promotion updatePromotion(Long promotionId, UpdatePromotionRequest request) {
        return promotionRepository.findById(promotionId)
                .map(existingPromotion -> updateExistingPromotion(existingPromotion, request))
                .map(promotionRepository::save)
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
    }

    private Promotion updateExistingPromotion(Promotion existingPromotion, UpdatePromotionRequest request) {
        existingPromotion.setPromotionName(request.getPromotionName());
        existingPromotion.setDescription(request.getDescription());
        existingPromotion.setCouponCode(request.getCouponCode());
        existingPromotion.setDiscountPercent(request.getDiscountPercent());
        existingPromotion.setDiscountAmount(request.getDiscountAmount());
        existingPromotion.setMinimumOrderValue(request.getMinimumOrderValue());
        existingPromotion.setStartDate(request.getStartDate());
        existingPromotion.setEndDate(request.getEndDate());
        existingPromotion.setIsActive(request.getIsActive());
        return existingPromotion;
    }

    @Override
    public List<Promotion> getAllPromotions() {
        return promotionRepository.findAll();
    }

    @Override
    public List<Promotion> getAllActivePromotions() {
        return List.of();
    }

    @Override
    public void deletePromotion(Long promotionId) {
        promotionRepository.findById(promotionId)
                .ifPresentOrElse(promotionRepository::delete, () -> {
                            throw new AppException(ErrorCode.PROMOTION_NOT_FOUND);
                        }
                );
    }

    @Override
    public PromotionDto convertToDto(Promotion promotion) {
        return modelMapper.map(promotion, PromotionDto.class);
    }

    @Override
    public List<PromotionDto> convertToDto(List<Promotion> promotions) {
        return promotions.stream()
                .map(this::convertToDto)
                .toList();
    }
}
