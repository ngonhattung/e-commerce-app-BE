package com.nhattung.promotionservice.service;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.entity.UserPromotion;
import com.nhattung.promotionservice.exception.AppException;
import com.nhattung.promotionservice.exception.ErrorCode;
import com.nhattung.promotionservice.repository.PromotionRepository;
import com.nhattung.promotionservice.repository.UserPromotionRepository;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.HandleUserPromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;
import com.nhattung.promotionservice.response.PageResponse;
import com.nhattung.promotionservice.utils.AuthenticatedUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PromotionService implements IPromotionService{

    private final PromotionRepository promotionRepository;
    private final UserPromotionRepository userPromotionRepository;
    private final AuthenticatedUser authenticatedUser;
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
                .isGlobal(request.getIsGlobal())
                .destroyed(false)
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
        // Kiểm tra trạng thái kích hoạt & thời gian
        if (!promotion.getIsActive()
                || (promotion.getStartDate() != null && promotion.getStartDate().isAfter(Instant.now()))
                || (promotion.getEndDate() != null && promotion.getEndDate().isBefore(Instant.now()))) {
            throw new AppException(ErrorCode.PROMOTION_NOT_ACTIVE);
        }

        // Kiểm tra xem khuyến mãi không phải là cá nhân
        if (!promotion.getIsGlobal()) {
            UserPromotion userPromotion = userPromotionRepository
                    .findByUserIdAndPromotionId(authenticatedUser.getUserId(), promotion.getId())
                    .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_AVAILABLE_FOR_USER));

            if (userPromotion.getIsUsed()) {
                throw new AppException(ErrorCode.PROMOTION_ALREADY_USED);
            }
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
        existingPromotion.setIsGlobal(request.getIsGlobal());
        return existingPromotion;
    }

    @Override
    public PageResponse<PromotionDto> getAllPromotions(int page, int size) {

        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<Promotion> promotionPage = promotionRepository.findByDestroyedFalse(pageable);

        List<PromotionDto> promotionDtos = promotionPage.getContent()
                .stream()
                .map(this::convertToDto)
                .toList();

        return PageResponse.<PromotionDto>builder()
                .currentPage(page)
                .totalPages(promotionPage.getTotalPages())
                .totalElements(promotionPage.getTotalElements())
                .pageSize(size)
                .data(promotionDtos)
                .build();

    }

    @Override
    public List<Promotion> getAllActivePromotions() {
        return List.of();
    }

    @Override
    public void deletePromotion(Long promotionId) {
       promotionRepository.findById(promotionId)
                .map(promotion -> {
                    promotion.setDestroyed(true);
                    return promotionRepository.save(promotion);
                })
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));

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

    @Override
    public void createUserPromotion(HandleUserPromotionRequest request) {
        UserPromotion userPromotion = UserPromotion.builder()
                .userId(request.getUserId())
                .promotionId(request.getPromotionId())
                .isUsed(false)
                .build();
        userPromotionRepository.save(userPromotion);
    }

    @Override
    public void updateUserPromotion(HandleUserPromotionRequest request) {
        UserPromotion userPromotion = userPromotionRepository
                .findByUserIdAndPromotionId(request.getUserId(), request.getPromotionId())
                .orElseThrow(() -> new AppException(ErrorCode.PROMOTION_NOT_FOUND));
        userPromotion.setIsUsed(true);
        userPromotionRepository.save(userPromotion);
    }
}
