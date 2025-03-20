package com.nhattung.userservice.service.userprofile;


import com.nhattung.event.dto.NotificationEvent;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
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
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ErrorNomalizer errorNomalizer;
    @Override
    public UserProfile createUserProfile(CreateUserProfileRequest request) {
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


        NotificationEvent notificationEvent = NotificationEvent.builder()
                .channel("email")
                .receiver(userProfile.getEmail())
                .templateCode("WELCOME_EMAIL")
                .params(Map.of(
                        "subject","Welcome to Dream Shop",
                        "content",formWelcomeEmailContent()
                ))
                .build();


            //Publish message to Kafka
            kafkaTemplate.send("notification-delivery",notificationEvent);

            return userProfileRepository.save(userProfile);

    }

    @Override
    public UserProfile getUserProfile(Long profileId) {
        return userProfileRepository.findById(profileId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }

    @Override
    public List<UserProfile> getAllUserProfiles() {
        return userProfileRepository.findAll();
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
    public List<UserProfileDto> convertToDto(List<UserProfile> userProfiles) {
        return userProfiles.stream()
                .map(this::convertToDto)
                .toList();
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

    public String formWelcomeEmailContent(){
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
                "            text-align: center;\n" +
                "            padding: 10px 0;\n" +
                "        }\n" +
                "        .header img {\n" +
                "            max-width: 150px;\n" +
                "        }\n" +
                "        .content {\n" +
                "            text-align: center;\n" +
                "            color: #333;\n" +
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
                "            <img src=\"https://your-logo-url.com/logo.png\" alt=\"Logo\">\n" +
                "        </div>\n" +
                "        <div class=\"content\">\n" +
                "            <h2>Welcome to Our E-Commerce Store!</h2>\n" +
                "            <p>Thank you for registering with us. We are excited to have you on board.</p>\n" +
                "            <p>Start exploring our wide range of products and enjoy exclusive discounts.</p>\n" +
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
