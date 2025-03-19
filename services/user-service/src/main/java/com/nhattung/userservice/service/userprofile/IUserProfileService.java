package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;

import java.util.List;

public interface IUserProfileService {
    UserProfile createUserProfile(CreateUserProfileRequest request);
    UserProfile getUserProfile(Long userId);
    List<UserProfile> getAllUserProfiles();
    UserProfile updateUserProfile(Long userId, UpdateUserProfileRequest request);
    void deleteUser(String userId);
    UserProfileDto convertToDto(UserProfile userProfile);
    List<UserProfileDto> convertToDto(List<UserProfile> userProfiles);
    void sendVerificationEmail(String userId);
    void forgotPassword(String email);
}
