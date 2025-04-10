package com.nhattung.userservice.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChangePasswordRequest {

    @NotBlank(message = "EMPTY_PASSWORD")
    @Size(min=8, message = "INVALID_PASSWORD")
    private String oldPassword;

    @NotBlank(message = "EMPTY_PASSWORD")
    @Size(min=8, message = "INVALID_PASSWORD")
    private String newPassword;

    @NotBlank(message = "EMPTY_EMAIL")
    @Email(message = "INVALID_EMAIL")
    private String email;
}
