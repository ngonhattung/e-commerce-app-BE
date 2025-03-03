package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.UserNotFoundException;
import com.nhattung.userservice.repository.UserProfileRepository;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserProfileService implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final ModelMapper modelMapper;
    @Override
    public UserProfile createUserProfile(CreateUserProfileRequest request) {
        UserProfile userProfile = UserProfile.builder()
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .gender(request.isGender())
                .avatar(request.getAvatar())
                .dateOfBirth(request.getDateOfBirth())
                .userId(request.getUserId())
                .build();
        return userProfileRepository.save(userProfile);

    }

    @Override
    public UserProfile getUserProfile(Long userId) {
        return userProfileRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public UserProfile updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        return Optional.ofNullable(getUserProfile(userId))
                .map(userProfile -> {
                    userProfile.setFullName(request.getFullName());
                    userProfile.setPhone(request.getPhone());
                    userProfile.setGender(request.isGender());
                    userProfile.setAvatar(request.getAvatar());
                    userProfile.setDateOfBirth(request.getDateOfBirth());
                    return userProfileRepository.save(userProfile);
                }).orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public void deleteUserProfile(Long userId) {
        userProfileRepository.findById(userId)
                .ifPresentOrElse(userProfileRepository::delete, () -> {
                    throw new UserNotFoundException("User not found");
                });
    }

    @Override
    public UserProfileDto convertToDto(UserProfile userProfile) {
        return modelMapper.map(userProfile, UserProfileDto.class);
    }
}
