package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.ChangePasswordRequest;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.ForgotPasswordRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserProfileService {
    UserProfile createUserProfile(CreateUserProfileRequest request, MultipartFile avatar);
    UserProfile getUserProfile();
    UserProfile getUserProfileById(String userId);
    List<UserProfile> getAllUserProfiles();
    PageResponse<UserProfileDto> getPagedUserProfiles(int page, int size);
    UserProfile updateUserProfile(UpdateUserProfileRequest request,MultipartFile avatar);
    void deleteUser();
    UserProfileDto convertToDto(UserProfile userProfile);
    List<UserProfileDto> convertToDto(List<UserProfile> userProfiles);
    void sendVerificationEmail();
    void forgotPassword(ForgotPasswordRequest request);
    boolean existsByEmail(String email);
    void changePassword(ChangePasswordRequest request);
    long getTotalUserCount();
}
