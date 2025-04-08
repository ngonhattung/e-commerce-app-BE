package com.nhattung.userservice.request;

import com.nhattung.userservice.entity.Address;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
@Data
public class UpdateUserProfileRequest {
    @NotBlank(message = "EMPTY_FULLNAME")
    @Size(min = 2, max = 100, message = "INVALID_FULLNAME")
    private String fullName;

    @NotBlank(message = "EMPTY_EMAIL")
    @Email(message = "INVALID_EMAIL")
    private String email;

    @NotBlank(message = "EMPTY_PASSWORD")
    @Size(min=8, message = "INVALID_PASSWORD")
    private String password;

    @NotBlank(message = "EMPTY_PHONE")
    @Pattern(regexp = "^[0-9]{10,15}$", message = "INVALID_PHONE")
    private String phone;

    @Past(message = "INVALID_BIRTHDAY")
    private LocalDate dateOfBirth;
    private boolean gender;
}
