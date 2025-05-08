package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.MonthlyRegistrationDto;
import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.request.*;
import com.nhattung.userservice.response.PageResponse;
import org.springframework.data.jpa.repository.Query;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IUserProfileService {
    UserProfile createUserProfile(CreateUserProfileRequest request, MultipartFile avatar);
    UserProfile createUserProfileByGoogleAndFacebook(CreateUserProfileGGFBRequest request);
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
    List<MonthlyRegistrationDto> getMonthlyRegistrationData();
    List<String> findUserIdsBySearchTerm(String searchTerm);
    List<String> findUserIdsByFullName(String fullName);
    List<String> findUserIdsByEmail(String email);
    List<String> findUserIdsByPhone(String phone);
}
