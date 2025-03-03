package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;

public interface IUserProfileService {
    UserProfile createUserProfile(CreateUserProfileRequest request);
    UserProfile getUserProfile(Long userId);
    UserProfile updateUserProfile(Long userId, UpdateUserProfileRequest request);
    void deleteUserProfile(Long userId);
    UserProfileDto convertToDto(UserProfile userProfile);

}
