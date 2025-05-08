package com.nhattung.promotionservice.controller;

import com.nhattung.promotionservice.dto.PromotionDto;
import com.nhattung.promotionservice.dto.PromotionSearchCriteria;
import com.nhattung.promotionservice.entity.Promotion;
import com.nhattung.promotionservice.request.CreatePromotionRequest;
import com.nhattung.promotionservice.request.HandleUserPromotionRequest;
import com.nhattung.promotionservice.request.UpdatePromotionRequest;
import com.nhattung.promotionservice.response.ApiResponse;
import com.nhattung.promotionservice.response.PageResponse;
import com.nhattung.promotionservice.service.IPromotionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
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
    public ApiResponse<PageResponse<PromotionDto>> getAllPromotions(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<PromotionDto> >builder()
                .message("Get all promotions successfully")
                .result(promotionService.getAllPromotions(page, size))
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

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/search")
    public ApiResponse<PageResponse<PromotionDto>> searchPromotions(
            @RequestParam(value = "searchTerm", required = false) String searchTerm,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PromotionSearchCriteria criteria = PromotionSearchCriteria.builder()
                .searchTerm(searchTerm)
                .build();
        PageResponse<PromotionDto> promotions = promotionService.searchPromotions(criteria, page, size);
        return ApiResponse.<PageResponse<PromotionDto>>builder()
                .message("Search promotions successfully")
                .result(promotions)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/filter")
    public ApiResponse<PageResponse<PromotionDto>> filterPromotions(
            @RequestParam(value = "promotionName", required = false) String promotionName,
            @RequestParam(value = "promotionCode", required = false) String promotionCode,
            @RequestParam(value = "status", required = false, defaultValue = "true") boolean status,
            @RequestParam(value = "startDatePromotionStartDate", required = false) String startDatePromotionStartDate,
            @RequestParam(value = "endDatePromotionStartDate", required = false) String endDatePromotionStartDate,
            @RequestParam(value = "startDatePromotionEndDate", required = false) String startDatePromotionEndDate,
            @RequestParam(value = "endDatePromotionEndDate", required = false) String endDatePromotionEndDate,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PromotionSearchCriteria criteria = PromotionSearchCriteria.builder()
                .promotionName(promotionName)
                .promotionCode(promotionCode)
                .status(status)
                .startDatePromotionStartDate(
                        startDatePromotionStartDate != null ? LocalDate.parse(startDatePromotionStartDate) : null)
                .endDatePromotionStartDate(
                        endDatePromotionStartDate != null ? LocalDate.parse(endDatePromotionStartDate) : null)
                .startDatePromotionEndDate(
                        startDatePromotionEndDate != null ? LocalDate.parse(startDatePromotionEndDate) : null)
                .endDatePromotionEndDate(
                        endDatePromotionEndDate != null ? LocalDate.parse(endDatePromotionEndDate) : null)
                .build();
        PageResponse<PromotionDto> promotions = promotionService.filterPromotions(criteria, page, size);
        return ApiResponse.<PageResponse<PromotionDto>>builder()
                .message("Filter promotions successfully")
                .result(promotions)
                .build();
    }
}
