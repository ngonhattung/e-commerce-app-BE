package com.nhattung.userservice.controller;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.ResourceNotFoundException;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.response.ApiResponse;
import com.nhattung.userservice.service.userprofile.IUserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user-profile")
public class UserProfileController {

    private final IUserProfileService userProfileService;


    @PostMapping("/add")
    public ResponseEntity<ApiResponse> createUserProfile(@RequestBody CreateUserProfileRequest request){
//        try {
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        UserProfile userProfile = userProfileService.createUserProfile(request);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ResponseEntity.ok(new ApiResponse("User profile created successfully", userProfileDto));
    }


    @GetMapping("/user/{userId}/profile")
    public ResponseEntity<ApiResponse> getUserProfile(@PathVariable Long userId){
        UserProfile userProfile = userProfileService.getUserProfile(userId);
        UserProfileDto userProfileDto = userProfileService.convertToDto(userProfile);
        return ResponseEntity.ok(new ApiResponse("User profile fetched successfully", userProfileDto));
    }


    @PutMapping("/user/{userId}/update")
    public ResponseEntity<ApiResponse> updateUserProfile(@PathVariable Long userId,
                                                         @RequestBody UpdateUserProfileRequest request){
        UserProfile userProfile = userProfileService.updateUserProfile(userId, request);
        return ResponseEntity.ok(new ApiResponse("User profile updated successfully", userProfile));
    }


    @DeleteMapping("/user/{userId}/delete")
    public ResponseEntity<ApiResponse> deleteUserProfile(@PathVariable Long userId){
        try {
            userProfileService.deleteUserProfile(userId);
            return ResponseEntity.ok(new ApiResponse("User profile deleted successfully", null));
        } catch (ResourceNotFoundException e){
            return ResponseEntity.status(NOT_FOUND).body(new ApiResponse(e.getMessage(), null));
        }
    }
}
