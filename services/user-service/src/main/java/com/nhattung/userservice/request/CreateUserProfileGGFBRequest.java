package com.nhattung.userservice.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateUserProfileGGFBRequest {
    private String userId;
    private String fullName;
    private String email;
    private String avatar;
}
