package com.nhattung.authservice.service.user;

import com.nhattung.authservice.dto.UserDto;
import com.nhattung.authservice.entity.User;
import com.nhattung.authservice.exception.AlreadyExistsException;
import com.nhattung.authservice.repository.UserRepository;
import com.nhattung.authservice.repository.httpclient.UserClient;
import com.nhattung.authservice.request.CreateUserProfileRequest;
import com.nhattung.authservice.request.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService implements IUserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;
    private final UserClient userClient;
    @Override
    public User createUser(RegisterRequest request) {
        return Optional.of(request)
                .filter(user -> !userRepository.existsByEmail(user.getEmail()))
                .map(req -> {
                    User user = User.builder()
                            .email(request.getEmail())
                            .password(passwordEncoder.encode(request.getPassword()))
                            .build();
                    user = userRepository.save(user);

                    //create user profile
                    var profileRequest = modelMapper.map(request, CreateUserProfileRequest.class);
                    profileRequest.setUserId(user.getId());
                    userClient.createUserProfile(profileRequest);

                    return user;
                }).orElseThrow(() -> new AlreadyExistsException("Oops!" +request.getEmail() +" already exists!"));
    }

    @Override
    public UserDto convertUserToDto(User user) {
        return modelMapper.map(user, UserDto.class);
    }


}
