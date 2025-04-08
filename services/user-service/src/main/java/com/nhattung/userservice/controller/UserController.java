package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final IUserProfileService userProfileService;

    @PostMapping(value = "/registration",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<UserProfileDto> createUserProfile(
            @Valid @ModelAttribute CreateUserProfileRequest request,
            @RequestParam(value = "avatar", required = false) MultipartFile avatar
    ) {
        UserProfile userProfile = userProfileService.createUserProfile(request,avatar);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ApiResponse.<UserProfileDto>builder()
                .message("User created successfully")
                .result(userProfileDto)
                .build();
    }
}
