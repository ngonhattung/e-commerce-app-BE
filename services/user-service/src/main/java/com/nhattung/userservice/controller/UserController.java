package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserProfileService userProfileService;

    @PostMapping("/registration")
    public ApiResponse<UserProfileDto> createUserProfile(@RequestBody CreateUserProfileRequest request) {
        UserProfile userProfile = userProfileService.createUserProfile(request);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User created successfully")
                .result(userProfileDto)
                .build();
    }
}
