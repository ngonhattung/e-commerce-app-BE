package com.nhattung.userservice.service.userprofile;

import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.ErrorNomalizer;
import com.nhattung.userservice.exception.UserNotFoundException;
import com.nhattung.userservice.repository.UserProfileRepository;
import com.nhattung.userservice.request.CreateUserProfileRequest;
import com.nhattung.userservice.request.UpdateUserProfileRequest;
import com.nhattung.userservice.utils.RandomUtil;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final ModelMapper modelMapper;
    @Value("${idp.client.realm}")
    private String REALM;
    private final Keycloak keycloak;
    private final RandomUtil randomUtil;
    private final ErrorNomalizer errorNomalizer;
    @Override
    public UserProfile createUserProfile(CreateUserProfileRequest request) {

            UserRepresentation userRepresentation = new UserRepresentation();
            userRepresentation.setUsername(request.getEmail());
            userRepresentation.setEmail(request.getEmail());
            userRepresentation.setFirstName(request.getFullName().split(" ")[0]);
            userRepresentation.setLastName(request.getFullName());
            userRepresentation.setEnabled(true);
            userRepresentation.setEmailVerified(false);

            CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
            credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
            credentialRepresentation.setValue(request.getPassword());
            credentialRepresentation.setTemporary(false);
            userRepresentation.setCredentials(List.of(credentialRepresentation));

            log.info("User Representation: {}", userRepresentation);

            UsersResource usersResource = getUsersResource();
            Response response = usersResource.create(userRepresentation);
            log.info("Status Code "+response.getStatus());

            if(!Objects.equals(201,response.getStatus())){
                String errorMessage = response.readEntity(String.class);
                throw errorNomalizer.handelKeyCloakException(new RuntimeException(errorMessage));
            }


            List<UserRepresentation> userRepresentations = usersResource.searchByEmail(request.getEmail(), true);
            UserRepresentation user = userRepresentations.get(0);

            //sendVerificationEmail(user.getId());

            UserProfile userProfile = UserProfile.builder()
                    .fullName(request.getFullName())
                    .email(request.getEmail())
                    .phone(request.getPhone())
                    .gender(request.isGender())
                    .avatar(request.getAvatar())
                    .dateOfBirth(request.getDateOfBirth())
                    .userId(user.getId())
                    .build();
            return userProfileRepository.save(userProfile);

    }

    @Override
    public UserProfile getUserProfile(Long profileId) {
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public UserProfile updateUserProfile(Long profileId, UpdateUserProfileRequest request) {
        return Optional.ofNullable(getUserProfile(profileId))
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
    public void deleteUser(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);
        userProfileRepository.findByUserId(userId)
                .ifPresentOrElse(userProfileRepository::delete, () -> {
                    throw new UserNotFoundException("User not found");
                });
    }

    @Override
    public UserProfileDto convertToDto(UserProfile userProfile) {
        return modelMapper.map(userProfile, UserProfileDto.class);
    }

    @Override
    public void sendVerificationEmail(String userId) {
        UsersResource usersResource = getUsersResource();
        usersResource.get(userId).sendVerifyEmail();
    }

    @Override
    public void forgotPassword(String email) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(email, true);
        UserRepresentation user = userRepresentations.get(0);
        usersResource.get(user.getId()).executeActionsEmail(List.of("UPDATE_PASSWORD"));
    }

    private UsersResource getUsersResource(){

        return keycloak.realm(REALM).users();
    }

}
