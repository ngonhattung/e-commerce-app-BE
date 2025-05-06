package com.nhattung.promotionservice.controller;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.HandleUserPromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;
import com.nhattung.promotionservice.response.ApiResponse;
import com.nhattung.promotionservice.service.IPromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/promotions")
public class PromotionController {

    private final IPromotionService promotionService;

    @PostMapping("/create")
    public ApiResponse<PromotionDto> savePromotion(@Valid @RequestBody CreatePromotionRequest request) {
        Promotion promotion = promotionService.savePromotion(request);
        PromotionDto promotionDto = promotionService.convertToDto(promotion);
        return ApiResponse.<PromotionDto>builder()
                .message("Create promotion successfully")
                .result(promotionDto)
                .build();
    }

    @PutMapping("/update/{promotionId}")
    public ApiResponse<PromotionDto> updatePromotion(
            @PathVariable("promotionId") Long promotionId,
            @Valid @RequestBody UpdatePromotionRequest request) {
        Promotion promotion = promotionService.updatePromotion(promotionId,request);
        PromotionDto promotionDto = promotionService.convertToDto(promotion);
        return ApiResponse.<PromotionDto>builder()
                .message("Update promotion successfully")
                .result(promotionDto)
                .build();
    }

    @GetMapping("/promotion/{promotionId}")
    public ApiResponse<PromotionDto> getPromotionById(@PathVariable("promotionId") Long promotionId) {
        Promotion promotion = promotionService.getPromotion(promotionId);
        PromotionDto promotionDto = promotionService.convertToDto(promotion);
        return ApiResponse.<PromotionDto>builder()
                .message("Get promotion successfully")
                .result(promotionDto)
                .build();
    }

    @GetMapping("/promotion-code/{promotionCode}")
    public ApiResponse<PromotionDto> getPromotionByCode(@PathVariable("promotionCode") String promotionCode) {
        Promotion promotion = promotionService.getPromotionByCouponCode(promotionCode);
        PromotionDto promotionDto = promotionService.convertToDto(promotion);
        return ApiResponse.<PromotionDto>builder()
                .message("Get promotion successfully")
                .result(promotionDto)
                .build();
    }

    @GetMapping("/active/{promotionCode}")
    public ApiResponse<PromotionDto> getActivePromotionByCode(@PathVariable("promotionCode") String promotionCode) {
        Promotion promotion = promotionService.getPromotionActiveByCouponCode(promotionCode);
        PromotionDto promotionDto = promotionService.convertToDto(promotion);
        return ApiResponse.<PromotionDto>builder()
                .message("Get active promotion successfully")
                .result(promotionDto)
                .build();
    }

    @GetMapping("/all")
    public ApiResponse<List<PromotionDto> > getAllPromotions() {
        List<Promotion> listPromotion = promotionService.getAllPromotions();
        List<PromotionDto> listPromotionDto = promotionService.convertToDto(listPromotion);
        return ApiResponse.<List<PromotionDto> >builder()
                .message("Get all promotions successfully")
                .result(listPromotionDto)
                .build();
    }

    @DeleteMapping("/delete/{promotionId}")
    public ApiResponse<String> deletePromotion(@PathVariable("promotionId") Long promotionId) {
        promotionService.deletePromotion(promotionId);
        return ApiResponse.<String>builder()
                .message("Delete promotion successfully")
                .build();
    }

    @PostMapping("/create-user-promotion")
    public ApiResponse<Void> createUserPromotion(@RequestBody HandleUserPromotionRequest request) {
        promotionService.createUserPromotion(request);
        return ApiResponse.<Void>builder()
                .message("Create user promotion successfully")
                .build();
    }

    @PutMapping("/update-user-promotion")
    public ApiResponse<Void> updateUserPromotion(@RequestBody HandleUserPromotionRequest request) {
        promotionService.updateUserPromotion(request);
        return ApiResponse.<Void>builder()
                .message("Update user promotion successfully")
                .build();
    }
}
