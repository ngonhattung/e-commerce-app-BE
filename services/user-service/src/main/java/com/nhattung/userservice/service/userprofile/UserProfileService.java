package com.nhattung.userservice.service.userprofile;


import com.nhattung.event.dto.NotificationEvent;
import com.nhattung.userservice.dto.UserProfileDto;
import com.nhattung.userservice.entity.UserProfile;
import com.nhattung.userservice.exception.AppException;
import com.nhattung.userservice.exception.ErrorCode;
import com.nhattung.userservice.exception.ErrorNomalizer;
import com.nhattung.userservice.repository.UserProfileRepository;
import com.nhattung.userservice.repository.httpclient.CartClient;
import com.nhattung.userservice.repository.httpclient.PromotionClient;
import com.nhattung.userservice.request.*;
import com.nhattung.userservice.response.PageResponse;
import com.nhattung.userservice.utils.AuthenticatedUser;
import com.nhattung.userservice.utils.RandomUtil;
import com.nhattung.userservice.utils.UploadToS3;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserProfileService implements IUserProfileService {

    private final UserProfileRepository userProfileRepository;
    private final PromotionClient promotionClient;
    private final ModelMapper modelMapper;
    @Value("${idp.client.realm}")
    private String REALM;
    @Value("${idp.client.id}")
    private String CLIENT_ID;
    @Value("${idp.client.secret}")
    private String CLIENT_SECRET;
    private final Keycloak keycloak;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ErrorNomalizer errorNomalizer;
    private final CartClient cartClient;
    private final AuthenticatedUser authenticatedUser;
    private final UploadToS3 uploadToS3;

    @Override
    public UserProfile createUserProfile(CreateUserProfileRequest request, MultipartFile avatar) {
        String fullName = request.getFullName().trim();
        String[] nameParts = fullName.split(" ");
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(request.getEmail());
        userRepresentation.setEmail(request.getEmail());
        userRepresentation.setFirstName(nameParts[0]);
        userRepresentation.setLastName(nameParts.length > 1 ? fullName.substring(fullName.indexOf(" ") + 1) : "");
        userRepresentation.setEnabled(true);
        userRepresentation.setEmailVerified(true);

        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(request.getPassword());
        credentialRepresentation.setTemporary(false);
        userRepresentation.setCredentials(List.of(credentialRepresentation));

        log.info("User Representation: {}", userRepresentation);

        UsersResource usersResource = getUsersResource();
        Response response = usersResource.create(userRepresentation);
        log.info("Status Code " + response.getStatus());

        if (!Objects.equals(201, response.getStatus())) {
            String errorMessage = response.readEntity(String.class);
            throw errorNomalizer.handelKeyCloakException(new RuntimeException(errorMessage));
        }


        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(request.getEmail(), true);
        UserRepresentation user = userRepresentations.get(0);


        //sendVerificationEmail(user.getId());
        //Upload avatar to S3
        String avatarUrl = "";
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = uploadToS3.uploadAvatarToS3(avatar);
        }
        UserProfile userProfile = UserProfile.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .gender(request.isGender())
                .avatar(avatarUrl)
                .dateOfBirth(request.getDateOfBirth())
                .userId(user.getId())
                .build();

        //Initialize cart for user
        cartClient.initializeCart(user.getId());

        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("email")
                .receiver(request.getEmail())
                .templateCode("WELCOME_EMAIL")
                .params(Map.of(
                        "subject", "Welcome to Dream Shop",
                        "content", formWelcomeEmailContent()
                ))
                .build();


        //Create user promotion
        promotionClient.createUserPromotion(
                HandleUserPromotionRequest.builder()
                        .userId(user.getId())
                        .promotionId(2L)
                        .build()
        );

        //Publish message to Kafka
        kafkaTemplate.send("notification-delivery", notificationEvent);



        return userProfileRepository.save(userProfile);

    }

    @Override
    public UserProfile getUserProfile() {
        return userProfileRepository.findByUserId(authenticatedUser.getUserId())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public UserProfile getUserProfileById(String userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
    }

    @Override
    public PageResponse<UserProfileDto> getPagedUserProfiles(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new AppException(ErrorCode.INVALID_PAGE_SIZE);
        }

        Sort sort = Sort.by(Sort.Direction.DESC, "createdAt");
        Pageable pageable = PageRequest.of(page - 1, size, sort);

        Page<UserProfile> userProfilePage = userProfileRepository.findAll(pageable);
        List<UserProfileDto> userProfileDtos = convertToDto(userProfilePage.getContent());

        return PageResponse.<UserProfileDto>builder()
                .currentPage(page)
                .totalPages(userProfilePage.getTotalPages())
                .totalElements(userProfilePage.getTotalElements())
                .pageSize(userProfilePage.getSize())
                .data(userProfileDtos)
                .build();

    }

    @Override
    public UserProfile updateUserProfile(UpdateUserProfileRequest request, MultipartFile avatar) {
        String avatarUrl;
        UserProfile existingUserProfile = getUserProfile();
        if (avatar != null && !avatar.isEmpty()) {
            avatarUrl = uploadToS3.uploadAvatarToS3(avatar);
        } else {
            avatarUrl = existingUserProfile.getAvatar();
        }
        return Optional.ofNullable(existingUserProfile)
                .map(userProfile -> {
                    userProfile.setFullName(request.getFullName());
                    userProfile.setPhone(request.getPhone());
                    userProfile.setGender(request.isGender());
                    userProfile.setAvatar(avatarUrl);
                    userProfile.setDateOfBirth(request.getDateOfBirth());
                    return userProfileRepository.save(userProfile);
                }).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXISTED));
    }

    @Override
    public void deleteUser() {
        String userId = authenticatedUser.getUserId();
        UsersResource usersResource = getUsersResource();
        usersResource.delete(userId);
        userProfileRepository.findByUserId(userId)
                .ifPresentOrElse(userProfileRepository::delete, () -> {
                    throw new AppException(ErrorCode.USER_NOT_EXISTED);
                });
    }

    @Override
    public UserProfileDto convertToDto(UserProfile userProfile) {
        return modelMapper.map(userProfile, UserProfileDto.class);
    }

    @Override
    public List<UserProfileDto> convertToDto(List<UserProfile> userProfiles) {
        return userProfiles.stream()
                .map(this::convertToDto)
                .toList();
    }

    @Override
    public void sendVerificationEmail() {
        UsersResource usersResource = getUsersResource();
        usersResource.get(authenticatedUser.getUserId()).sendVerifyEmail();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(request.getEmail(), true);
        if (userRepresentations.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        UserRepresentation user = userRepresentations.get(0);
        String userId = user.getId();
        CredentialRepresentation credentialRepresentation = new CredentialRepresentation();
        credentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        credentialRepresentation.setValue(request.getNewPassword());
        credentialRepresentation.setTemporary(false);
        usersResource.get(userId).resetPassword(credentialRepresentation);
    }

    @Override
    public boolean existsByEmail(String email) {
        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(email, true);
        return !userRepresentations.isEmpty();
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {

        UsersResource usersResource = getUsersResource();
        List<UserRepresentation> userRepresentations = usersResource.searchByEmail(request.getEmail(), true);
        if (userRepresentations.isEmpty()) {
            throw new AppException(ErrorCode.USER_NOT_EXISTED);
        }
        UserRepresentation user = userRepresentations.get(0);
        String userId = user.getId();


        //verify old password
        try {
            Keycloak keycloakVerify = KeycloakBuilder.builder()
                    .serverUrl("http://localhost:8081")
                    .realm(REALM)
                    .clientId(CLIENT_ID)
                    .clientSecret(CLIENT_SECRET)
                    .username(request.getEmail())
                    .password(request.getOldPassword())
                    .grantType(OAuth2Constants.PASSWORD)
                    .build();

            keycloakVerify.tokenManager().getAccessToken(); // ép login
        } catch (Exception ex) {
            throw new AppException(ErrorCode.OLD_PASSWORD_NOT_MATCH);
        }


        //update password
        CredentialRepresentation newCredentialRepresentation = new CredentialRepresentation();
        newCredentialRepresentation.setType(CredentialRepresentation.PASSWORD);
        newCredentialRepresentation.setValue(request.getNewPassword());
        newCredentialRepresentation.setTemporary(false);
        usersResource.get(userId).resetPassword(newCredentialRepresentation);
    }

    @Override
    public long getTotalUserCount() {
        return userProfileRepository.count();
    }

    private UsersResource getUsersResource() {

        return keycloak.realm(REALM).users();
    }


    public String formWelcomeEmailContent() {
        return "<!DOCTYPE html>\n" +
                "<html>\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>Welcome to Our E-Commerce Store</title>\n" +
                "    <style>\n" +
                "        body {\n" +
                "            font-family: Arial, sans-serif;\n" +
                "            background-color: #f4f4f4;\n" +
                "            margin: 0;\n" +
                "            padding: 0;\n" +
                "        }\n" +
                "        .container {\n" +
                "            max-width: 600px;\n" +
                "            margin: 20px auto;\n" +
                "            background: #ffffff;\n" +
                "            padding: 20px;\n" +
                "            border-radius: 10px;\n" +
                "            box-shadow: 0px 0px 10px rgba(0, 0, 0, 0.1);\n" +
                "        }\n" +
                "        .header {\n" +
                "            display: flex;\n" +
                "            justify-content: center;\n" +
                "            align-items: center;\n" +
                "            padding: 10px 0;\n" +
                "            width: 100%;\n" +
                "        }\n" +
                "        .header img {\n" +
                "            max-width: 300px;\n" +
                "            height: auto;\n" +
                "            display: block;\n" +
                "            margin: 0 auto;\n" +
                "        }\n" +
                "        .content {\n" +
                "            text-align: center;\n" +
                "            color: #333;\n" +
                "        }\n" +
                "        .promo-code {\n" +
                "            background-color: #e0f7fa;\n" +
                "            border: 1px dashed #00acc1;\n" +
                "            padding: 10px;\n" +
                "            margin: 20px auto;\n" +
                "            display: inline-block;\n" +
                "            font-weight: bold;\n" +
                "            color: #007c91;\n" +
                "            border-radius: 5px;\n" +
                "        }\n" +
                "        .button {\n" +
                "            display: inline-block;\n" +
                "            background: #ff6600;\n" +
                "            color: #ffffff;\n" +
                "            padding: 10px 20px;\n" +
                "            text-decoration: none;\n" +
                "            border-radius: 5px;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "        .footer {\n" +
                "            text-align: center;\n" +
                "            font-size: 12px;\n" +
                "            color: #777;\n" +
                "            margin-top: 20px;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <div class=\"container\">\n" +
                "        <div class=\"header\">\n" +
                "            <img src=\"https://res.cloudinary.com/dclf0ngcu/image/upload/v1743265120/dreamy-mart/logo-blue_cnfw0g.png\" alt=\"Logo\">\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Welcome to Dream E-Commerce Store!</h2>\n" +
                "            <p>Thank you for registering with us. We are excited to have you on board.</p>\n" +
                "            <p>Start exploring our wide range of products and enjoy exclusive discounts.</p>\n" +
                "            <p>Use the promo code below to get <strong>10% off</strong> your first order:</p>\n" +
                "            <div class=\"promo-code\">WELCOMEDREAMYMART</div>\n" +
                "            <a href=\"https://your-ecommerce-website.com\" class=\"button\">Shop Now</a>\n" +
                "        </div>\n" +
                "        <div class=\"footer\">\n" +
                "            <p>&copy; 2025 Your E-Commerce Store. All rights reserved.</p>\n" +
                "        </div>\n" +
                "    </div>\n" +
                "</body>\n" +
                "</html>";
    }

}
