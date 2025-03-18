package com.nhattung.userservice.request;

import com.nhattung.userservice.entity.Address;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserProfileRequest {
    private String fullName;
    private String email;
    private String password;
    private String phone;
    private String avatar;
    private LocalDate dateOfBirth;
    private boolean gender;
}
