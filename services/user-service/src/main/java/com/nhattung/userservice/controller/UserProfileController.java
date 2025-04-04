package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.AppException;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.response.PageResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-profile")
public class UserProfileController {

    private final IUserProfileService userProfileService;

    @GetMapping("/user/{userId}/profile")
    public ApiResponse<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User profile fetched successfully")
                .result(userProfileDto)
                .build();
    }

    @PreAuthorize("hasRole('ROLE_ADMIN')")
    @GetMapping("/profiles")
    public ApiResponse<PageResponse<UserProfileDto>> getAllUserProfiles(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        return ApiResponse.<PageResponse<UserProfileDto>>builder()
                .message("User profiles fetched successfully")
                .result(userProfileService.getPagedUserProfiles(page, size))
                .build();
    }

    @PutMapping("/user/{userId}/update")
    public ApiResponse<UserProfileDto> updateUserProfile(@PathVariable Long userId,
                                                         @RequestBody UpdateUserProfileRequest request) {
        UserProfile userProfile = userProfileService.updateUserProfile(userId, request);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User profile updated successfully")
                .result(userProfileDto)
                .build();
    }


    @DeleteMapping("/user/{userId}/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUserProfile(@PathVariable String userId) {
        try {
            userProfileService.deleteUser(userId);
            return ResponseEntity.ok(
                    ApiResponse.<Void>builder()
                            .message("User profile deleted successfully")
                            .result(null)
                            .build()
            );
        } catch (AppException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
