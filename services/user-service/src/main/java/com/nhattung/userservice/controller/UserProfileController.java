package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.ResourceNotFoundException;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user-profile")
public class UserProfileController {

    private final IUserProfileService userProfileService;


    @PostMapping("/registration")
    public ApiResponse<UserProfileDto> createUserProfile(@RequestBody CreateUserProfileRequest request) {
        UserProfile userProfile = userProfileService.createUserProfile(request);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User profile created successfully")
                .result(userProfileDto)
                .build();
    }


    @GetMapping("/user/{userId}/profile")
    public ApiResponse<UserProfileDto> getUserProfile(@PathVariable Long userId) {
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User profile fetched successfully")
                .result(userProfileDto)
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
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<Void>builder()
                            .message(e.getMessage())
                            .result(null)
                            .build());
        }
    }
}
