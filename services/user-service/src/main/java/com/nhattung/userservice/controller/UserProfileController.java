package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.AppException;
import com.nhattung.userservice.request.ChangePasswordRequest;
import com.nhattung.userservice.request.ForgotPasswordRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.response.PageResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-profile")
public class UserProfileController {

    private final IUserProfileService userProfileService;

    @GetMapping("/user/profile")
    public ApiResponse<UserProfileDto> getUserProfile() {
        UserProfile userProfile = userProfileService.getUserProfile();
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

    @PutMapping(value = "/user/update",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileDto> updateUserProfile(
            @Valid @ModelAttribute UpdateUserProfileRequest request,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserProfile userProfile = userProfileService.updateUserProfile(request,avatar);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User profile updated successfully")
                .result(userProfileDto)
                .build();
    }


    @DeleteMapping("/user/delete")
    public ResponseEntity<ApiResponse<Void>> deleteUserProfile() {
        try {
            userProfileService.deleteUser();
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

    @PostMapping("/user/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userProfileService.forgotPassword(request);
        return ApiResponse.<Void>builder()
                        .message("Change password successfully")
                        .result(null)
                        .build();
    }

    @PostMapping("/user/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        userProfileService.changePassword(request);
        return ApiResponse.<Void>builder()
                .message("Change password successfully")
                .result(null)
                .build();
    }
    @GetMapping("/user/verify-email/{email}")
    public ApiResponse<Boolean> sendVerificationEmail(@PathVariable(value = "email") String email) {
        return ApiResponse.<Boolean>builder()
                .message("Verification email sent successfully")
                .result(userProfileService.existsByEmail(email))
                .build();
    }
}
