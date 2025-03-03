package com.nhattung.authservice.service.user;

import com.nhattung.authservice.dto.UserDto;
import com.nhattung.authservice.entity.User;
import com.nhattung.authservice.request.CreateUserProfileRequest;
import com.nhattung.authservice.request.RegisterRequest;

public interface IUserService {

    User createUser(RegisterRequest request);
    UserDto convertUserToDto(User user);
}
